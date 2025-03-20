package com.example.nearfind.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.ActivityCompat
import com.example.nearfind.NearFindApplication
import com.example.nearfind.bluetooth.BleScanner
import com.example.nearfind.data.model.DistanceCategory
import com.example.nearfind.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class BluetoothScanService : Service() {

    // Obtenemos instancias de los managers a través del AppContainer
    private lateinit var bleScanner: BleScanner
    private lateinit var notificationService: NotificationService

    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private var scanJob: Job? = null
    private var monitorJob: Job? = null
    private val isRunning = AtomicBoolean(false)
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()

        // Inicialización de dependencias desde AppContainer
        val appContainer = (application as NearFindApplication).appContainer
        bleScanner = appContainer.bleScanner
        notificationService = NotificationService(applicationContext)

        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isRunning.compareAndSet(false, true)) {
            // Verificar permisos antes de iniciar el servicio en primer plano
            if (!hasRequiredPermissions()) {
                stopSelf()
                return START_NOT_STICKY
            }

            startForeground(
                Constants.SCANNING_NOTIFICATION_ID,
                notificationService.createScanningNotification()
            )

            startScanning()
            startMonitoring()
        }

        return START_STICKY
    }

    private fun hasRequiredPermissions(): Boolean {
        // Verificar permiso FOREGROUND_SERVICE_LOCATION (obligatorio para Android 14)
        val hasForegroundServiceLocation =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.FOREGROUND_SERVICE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            } else true

        // Verificar al menos uno de los permisos de ubicación
        val hasLocationPermission = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

        return hasForegroundServiceLocation && hasLocationPermission
    }

    private fun startScanning() {
        scanJob = serviceScope.launch {
            while (isRunning.get()) {
                bleScanner.startScan()
                delay(Constants.SCAN_PERIOD_MS)
                bleScanner.stopScan()
                delay(Constants.SCAN_INTERVAL_MS)
            }
        }
    }

    private fun startMonitoring() {
        monitorJob = serviceScope.launch {
            bleScanner.nearbyDevices.collectLatest { devices ->
                devices.values.forEach { device ->
                    if (device.getDistanceCategory() == DistanceCategory.CLOSE) {
                        notificationService.showDeviceDetectionNotification(device)
                    }
                }
            }
        }
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "NearFind::BluetoothScanWakeLock"
        )
        wakeLock?.acquire(10 * 60 * 1000L) // 10 minutes max
    }

    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        wakeLock = null
    }

    override fun onDestroy() {
        isRunning.set(false)
        scanJob?.cancel()
        monitorJob?.cancel()
        bleScanner.stopScan()
        releaseWakeLock()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}