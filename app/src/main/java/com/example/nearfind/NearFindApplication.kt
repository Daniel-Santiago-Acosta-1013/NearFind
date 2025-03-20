package com.example.nearfind

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.nearfind.di.AppContainer

class NearFindApplication : Application() {

    // Inicialización del contenedor de dependencias
    lateinit var appContainer: AppContainer
        private set

    // Singleton para acceso global
    companion object {
        lateinit var instance: NearFindApplication
            private set

        const val SCANNING_CHANNEL_ID = "scanning_channel"
        const val DEVICE_DETECTION_CHANNEL_ID = "device_detection_channel"
        const val PAIRING_REQUEST_CHANNEL_ID = "pairing_request_channel"
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        appContainer = AppContainer(applicationContext)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val scanningChannel = NotificationChannel(
                SCANNING_CHANNEL_ID,
                "Bluetooth Scanning",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Used for the ongoing Bluetooth scan service"
            }

            val deviceDetectionChannel = NotificationChannel(
                DEVICE_DETECTION_CHANNEL_ID,
                "Device Detection",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Used for notifications when a device is detected nearby"
            }

            val pairingRequestChannel = NotificationChannel(
                PAIRING_REQUEST_CHANNEL_ID,
                "Pairing Requests",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Used for notifications when a pairing request is received"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(scanningChannel)
            notificationManager.createNotificationChannel(deviceDetectionChannel)
            notificationManager.createNotificationChannel(pairingRequestChannel)
        }
    }
}