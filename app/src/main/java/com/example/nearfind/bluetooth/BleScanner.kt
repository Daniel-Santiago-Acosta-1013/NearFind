package com.example.nearfind.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.example.nearfind.data.model.NearbyDevice
import com.example.nearfind.data.model.UserData
import com.example.nearfind.data.repository.UserRepository
import com.example.nearfind.util.toNearbyDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class BleScanner(
    private val context: Context,
    private val userRepository: UserRepository
) {
    private val _scanState = MutableStateFlow(false)
    val scanState: StateFlow<Boolean> = _scanState.asStateFlow()

    private val _nearbyDevices = MutableStateFlow<Map<String, NearbyDevice>>(emptyMap())
    val nearbyDevices: StateFlow<Map<String, NearbyDevice>> = _nearbyDevices.asStateFlow()

    private val deviceMap = ConcurrentHashMap<String, NearbyDevice>()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // Obtener el adaptador Bluetooth del sistema
    private val bluetoothAdapter by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? android.bluetooth.BluetoothManager
        bluetoothManager?.adapter
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val rssi = result.rssi

            coroutineScope.launch {
                try {
                    // Verificar si el dispositivo ya está emparejado
                    val isPaired = userRepository.isDevicePaired(device.address)

                    // Aquí podrías obtener más información del dispositivo, como datos del usuario
                    // Esto requeriría una implementación más compleja para leer características BLE
                    // Por ahora, simulamos con datos básicos
                    val userData = if (isPaired) {
                        UserData(
                            userId = device.address,
                            name = device.name ?: "Usuario emparejado",
                            isProfessional = false
                        )
                    } else {
                        null
                    }

                    val nearbyDevice = device.toNearbyDevice(rssi, userData, isPaired)
                    deviceMap[nearbyDevice.id] = nearbyDevice
                    _nearbyDevices.update { deviceMap }
                } catch (e: SecurityException) {
                    // Manejar excepción de permisos
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            stopScan()
        }
    }

    fun startScan() {
        if (bluetoothAdapter?.isEnabled != true) {
            return
        }

        // Verificar permisos antes de proceder
        if (!hasRequiredPermissions()) {
            return
        }

        try {
            startScanWithPermissionCheck()
        } catch (e: SecurityException) {
            _scanState.value = false
        }
    }

    @SuppressLint("MissingPermission")
    private fun startScanWithPermissionCheck() {
        // Verificar permisos de nuevo para estar seguros
        if (!hasRequiredPermissions()) {
            return
        }

        try {
            // Esta función solo se llama después de verificar permisos
            val scanFilter = ScanFilter.Builder()
                // Opcionalmente filtrar por Service UUID
                // .setServiceUuid(ParcelUuid(UUID.fromString(Constants.PAIRING_SERVICE_UUID)))
                .build()

            val scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            bluetoothAdapter?.bluetoothLeScanner?.startScan(
                listOf(scanFilter),
                scanSettings,
                scanCallback
            )

            _scanState.value = true
        } catch (e: SecurityException) {
            _scanState.value = false
        }
    }

    fun stopScan() {
        if (!hasRequiredPermissions()) {
            _scanState.value = false
            return
        }

        try {
            stopScanWithPermissionCheck()
        } catch (e: SecurityException) {
            _scanState.value = false
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopScanWithPermissionCheck() {
        if (!hasRequiredPermissions()) {
            _scanState.value = false
            return
        }

        try {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        } catch (e: SecurityException) {
            // Manejar excepción de permisos
        } finally {
            _scanState.value = false
        }
    }

    fun clearDevices() {
        deviceMap.clear()
        _nearbyDevices.update { emptyMap() }
    }

    private fun hasRequiredPermissions(): Boolean {
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        return requiredPermissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    companion object {
        private var instance: BleScanner? = null

        fun getInstance(
            context: Context,
            userRepository: UserRepository
        ): BleScanner {
            return instance ?: synchronized(this) {
                instance ?: BleScanner(
                    context.applicationContext,
                    userRepository
                ).also { instance = it }
            }
        }
    }
}