package com.example.nearfind.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothStatusCodes
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import android.bluetooth.BluetoothManager as AndroidBluetoothManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BleConnectionManager(private val context: Context) {
    private val bluetoothManager: AndroidBluetoothManager? by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? AndroidBluetoothManager
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }
    private var bluetoothGatt: BluetoothGatt? = null
    private var isConnected = false

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                isConnected = true
                try {
                    if (hasRequiredPermissions()) {
                        gatt.discoverServices()
                    }
                } catch (e: SecurityException) {
                    // Manejar la excepción de seguridad
                    isConnected = false
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                isConnected = false
                try {
                    if (hasRequiredPermissions()) {
                        gatt.close()
                    }
                } catch (e: SecurityException) {
                    // Manejar la excepción de seguridad
                }
                bluetoothGatt = null
            }
        }
    }

    suspend fun connect(deviceAddress: String): Boolean = withContext(Dispatchers.IO) {
        if (!hasRequiredPermissions()) {
            return@withContext false
        }

        return@withContext try {
            connectWithPermissionCheck(deviceAddress)
        } catch (e: SecurityException) {
            false
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun connectWithPermissionCheck(deviceAddress: String): Boolean = suspendCoroutine { continuation ->
        val device: BluetoothDevice? = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                try {
                    bluetoothAdapter?.getRemoteDevice(deviceAddress)
                } catch (e: SecurityException) {
                    continuation.resume(false)
                    return@suspendCoroutine
                } catch (e: IllegalArgumentException) {
                    continuation.resume(false)
                    return@suspendCoroutine
                }
            } else {
                try {
                    bluetoothAdapter?.getRemoteDevice(deviceAddress)
                } catch (e: IllegalArgumentException) {
                    continuation.resume(false)
                    return@suspendCoroutine
                }
            }
        } catch (e: SecurityException) {
            continuation.resume(false)
            return@suspendCoroutine
        }

        if (device == null) {
            continuation.resume(false)
            return@suspendCoroutine
        }

        try {
            bluetoothGatt = device.connectGatt(context, false, gattCallback)

            // Simulamos un tiempo de espera para la conexión
            // En una implementación real, utilizaríamos callbacks adecuados
            Thread.sleep(2000)

            continuation.resume(isConnected)
        } catch (e: SecurityException) {
            continuation.resume(false)
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        if (!hasRequiredPermissions()) {
            return
        }

        try {
            bluetoothGatt?.disconnect()
            bluetoothGatt = null
            isConnected = false
        } catch (e: SecurityException) {
            // Manejar la excepción de seguridad
        }
    }

    fun getService(uuid: UUID): BluetoothGattService? {
        if (!isConnected || !hasRequiredPermissions()) {
            return null
        }

        return try {
            bluetoothGatt?.getService(uuid)
        } catch (e: SecurityException) {
            null
        }
    }

    @SuppressLint("MissingPermission")
    fun writeCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        data: ByteArray
    ): Boolean {
        if (!isConnected || !hasRequiredPermissions()) {
            return false
        }

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                writeCharacteristicApi33(characteristic, data)
            } else {
                writeCharacteristicLegacy(characteristic, data)
            }
        } catch (e: SecurityException) {
            false
        }
    }

    @SuppressLint("MissingPermission")
    private fun writeCharacteristicApi33(
        characteristic: BluetoothGattCharacteristic,
        data: ByteArray
    ): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val result = bluetoothGatt?.writeCharacteristic(
                    characteristic,
                    data,
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                ) ?: BluetoothStatusCodes.ERROR_UNKNOWN

                return result == BluetoothStatusCodes.SUCCESS
            } catch (e: SecurityException) {
                return false
            }
        }
        return false
    }

    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    private fun writeCharacteristicLegacy(
        characteristic: BluetoothGattCharacteristic,
        data: ByteArray
    ): Boolean {
        try {
            characteristic.value = data
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            return bluetoothGatt?.writeCharacteristic(characteristic) ?: false
        } catch (e: SecurityException) {
            return false
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }

        return requiredPermissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        private var instance: BleConnectionManager? = null

        fun getInstance(context: Context): BleConnectionManager {
            return instance ?: synchronized(this) {
                instance ?: BleConnectionManager(context.applicationContext).also { instance = it }
            }
        }
    }
}