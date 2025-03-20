package com.example.nearfind.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.Context

class BluetoothManagerWrapper(private val context: Context) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    companion object {
        private var instance: BluetoothManagerWrapper? = null

        fun getInstance(context: Context): BluetoothManagerWrapper {
            return instance ?: synchronized(this) {
                instance ?: BluetoothManagerWrapper(context.applicationContext).also { instance = it }
            }
        }
    }
}