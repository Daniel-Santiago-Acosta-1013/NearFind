package com.example.nearfind.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.nearfind.MainActivity
import com.example.nearfind.NearFindApplication.Companion.DEVICE_DETECTION_CHANNEL_ID
import com.example.nearfind.NearFindApplication.Companion.PAIRING_REQUEST_CHANNEL_ID
import com.example.nearfind.NearFindApplication.Companion.SCANNING_CHANNEL_ID
import com.example.nearfind.R
import com.example.nearfind.data.model.NearbyDevice
import com.example.nearfind.data.model.PairingRequest
import com.example.nearfind.util.Constants

class NotificationService(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createScanningNotification(): Notification {
        val activityIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, SCANNING_CHANNEL_ID)
            .setContentTitle("NearFind activo")
            .setContentText("Buscando dispositivos cercanos...")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth) // Reemplazando el icono faltante
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    fun showDeviceDetectionNotification(device: NearbyDevice) {
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("DEVICE_ID", device.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            device.id.hashCode(),
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, DEVICE_DETECTION_CHANNEL_ID)
            .setContentTitle("Dispositivo cercano detectado")
            .setContentText("${device.name} está a ${device.getFormattedDistance()}")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth) // Usando icono del sistema
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(device.id.hashCode(), notification)
    }

    fun showPairingRequestNotification(request: PairingRequest) {
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("GOTO_PAIRING_REQUESTS", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            request.id.hashCode(),
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, PAIRING_REQUEST_CHANNEL_ID)
            .setContentTitle("Nueva solicitud de emparejamiento")
            .setContentText("${request.requesterName} quiere conectarse contigo")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth) // Usando icono del sistema
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(Constants.PAIRING_REQUEST_NOTIFICATION_ID + request.id.hashCode(), notification)
    }
}