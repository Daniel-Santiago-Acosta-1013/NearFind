package com.example.nearfind.di

import android.content.Context
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager as AndroidBluetoothManager
import com.example.nearfind.bluetooth.BleConnectionManager
import com.example.nearfind.bluetooth.BleScanner
import com.example.nearfind.bluetooth.PairingManager
import com.example.nearfind.data.repository.BluetoothRepository
import com.example.nearfind.data.repository.UserRepository
import com.example.nearfind.util.UserManager
import com.example.nearfind.util.DataStoreManager
import org.json.JSONObject

class AppContainer(private val applicationContext: Context) {

    // Get system Bluetooth adapter
    private val systemBluetoothManager by lazy {
        applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as? AndroidBluetoothManager
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        systemBluetoothManager?.adapter
    }

    // DataStore Manager
    val dataStoreManager by lazy {
        DataStoreManager(applicationContext)
    }

    // Managers
    val userManager by lazy {
        UserManager.getInstance(applicationContext)
    }

    // Repositories
    val userRepository by lazy {
        UserRepository(userManager = userManager, dataStoreManager = dataStoreManager)
    }

    // Bluetooth components
    val bleConnectionManager by lazy {
        BleConnectionManager(applicationContext)
    }

    val bleScanner by lazy {
        BleScanner(applicationContext, userRepository = userRepository)
    }

    val bluetoothRepository by lazy {
        BluetoothRepository(bleScanner = bleScanner, connectionManager = bleConnectionManager)
    }

    // JSON utility class
    class JSONConverter {
        fun <T> toJson(obj: T): String {
            // Implementation simplificada para diferentes tipos de objetos
            val jsonObject = JSONObject()
            when (obj) {
                is com.example.nearfind.data.model.User -> {
                    jsonObject.put("id", obj.id)
                    jsonObject.put("firstName", obj.firstName)
                    jsonObject.put("lastName", obj.lastName)
                    jsonObject.put("isProfessional", obj.isProfessional)
                    // Añadir más campos según sea necesario
                }
                is com.example.nearfind.data.model.UserData -> {
                    jsonObject.put("userId", obj.userId)
                    jsonObject.put("name", obj.name)
                    jsonObject.put("isProfessional", obj.isProfessional)
                }
                // Añadir más tipos según sea necesario
            }
            return jsonObject.toString()
        }

        inline fun <reified T> fromJson(json: String): T? {
            try {
                val jsonObject = JSONObject(json)
                return when (T::class.java) {
                    com.example.nearfind.data.model.UserData::class.java -> {
                        com.example.nearfind.data.model.UserData(
                            userId = jsonObject.getString("userId"),
                            name = jsonObject.getString("name"),
                            isProfessional = jsonObject.getBoolean("isProfessional")
                        ) as T
                    }
                    // Añadir más tipos según sea necesario
                    else -> null
                }
            } catch (e: Exception) {
                return null
            }
        }
    }

    val jsonConverter by lazy {
        JSONConverter()
    }

    // Bluetooth Services
    val pairingManager by lazy {
        PairingManager(
            applicationContext,
            bleConnectionManager,
            userRepository
        )
    }
}