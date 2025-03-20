package com.example.nearfind.util

import android.bluetooth.BluetoothDevice
import com.example.nearfind.data.model.NearbyDevice
import com.example.nearfind.data.model.UserData
import kotlin.math.pow

fun BluetoothDevice.toNearbyDevice(rssi: Int, userData: UserData? = null, isPaired: Boolean = false): NearbyDevice {
    val distance = calculateDistance(rssi)
    return NearbyDevice(
        id = address,
        name = name ?: "Unknown Device",
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