package com.example.nearfind.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
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
            startForeground(
                Constants.SCANNING_NOTIFICATION_ID,
                notificationService.createScanningNotification()
            )

            startScanning()
            startMonitoring()
        }

        return START_STICKY
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