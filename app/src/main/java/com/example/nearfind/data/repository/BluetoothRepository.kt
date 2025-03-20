package com.example.nearfind.data.repository

import com.example.nearfind.bluetooth.BleConnectionManager
import com.example.nearfind.bluetooth.BleScanner
import com.example.nearfind.data.model.ConnectionState
import com.example.nearfind.data.model.NearbyDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

class BluetoothRepository(
    private val bleScanner: BleScanner,
    private val connectionManager: BleConnectionManager
) {
    // Scanner
    val isScanningFlow: StateFlow<Boolean> = bleScanner.scanState

    val nearbyDevicesFlow: Flow<List<NearbyDevice>> = bleScanner.nearbyDevices.map { it.values.toList() }

    // Crear nuestros propios StateFlow para manejar el estado de conexión
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionStateFlow: StateFlow<ConnectionState> = _connectionState

    // Crear nuestro propio StateFlow para rastrear el dispositivo conectado
    private val _connectedDeviceId = MutableStateFlow<String?>(null)
    val connectedDeviceIdFlow: StateFlow<String?> = _connectedDeviceId

    fun startScan() {
        bleScanner.startScan()
    }

    fun stopScan() {
        bleScanner.stopScan()
    }

    // Suspendemos la función para manejar la conexión a través de una corrutina
    suspend fun connectToDevice(deviceId: String) {
        _connectionState.value = ConnectionState.CONNECTING

        val connected = connectionManager.connect(deviceId)

        if (connected) {
            _connectionState.value = ConnectionState.CONNECTED
            _connectedDeviceId.value = deviceId
        } else {
            _connectionState.value = ConnectionState.DISCONNECTED
            _connectedDeviceId.value = null
        }
    }

    fun disconnectFromDevice() {
        connectionManager.disconnect()
        _connectionState.value = ConnectionState.DISCONNECTED
        _connectedDeviceId.value = null
    }

    fun getDeviceById(deviceId: String): NearbyDevice? {
        return bleScanner.nearbyDevices.value[deviceId]
    }
}