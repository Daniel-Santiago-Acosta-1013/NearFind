package com.example.nearfind.util

import android.bluetooth.BluetoothDevice
import android.util.Log
import com.example.nearfind.data.model.NearbyDevice
import com.example.nearfind.data.model.UserData
import kotlin.math.pow

fun BluetoothDevice.toNearbyDevice(rssi: Int, userData: UserData? = null, isPaired: Boolean = false): NearbyDevice {
    val distance = calculateDistance(rssi)

    // Valores predeterminados en caso de que no podamos acceder a las propiedades del dispositivo
    var deviceAddress = "unknown_address"
    var deviceName = "Unknown Device"

    try {
        // Intentar acceder a las propiedades que requieren permiso
        deviceAddress = address
        deviceName = name ?: "Unknown Device"
    } catch (e: SecurityException) {
        // Registrar el error para depuración
        Log.e("BluetoothExtensions", "Error de permiso al acceder a información del dispositivo Bluetooth", e)

        // Intentar obtener información básica del dispositivo a través de su toString()
        // que puede contener parte del nombre o dirección sin necesidad de permiso específico
        val deviceString = toString()
        if (deviceString.contains(":") && deviceString.length >= 17) {
            // Podría ser una dirección MAC
            deviceAddress = deviceString.takeLast(17)
        }

        deviceName = "Dispositivo (sin permiso)"
    }

    return NearbyDevice(
        id = deviceAddress,
        name = deviceName,
        rssi = rssi,
        distance = distance,
        lastSeen = System.currentTimeMillis(),
        userData = userData,
        isPaired = isPaired
    )
}

fun calculateDistance(rssi: Int): Double {
    // Use the log-distance path loss model
    // d = 10 ^ ((RSSI_at_1m - RSSI) / (10 * n))
    // where n is the environmental factor
    return 10.0.pow(
        (Constants.RSSI_AT_ONE_METER - rssi) / (10 * Constants.ENVIRONMENTAL_FACTOR)
    )
}