package com.example.nearfind.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlin.math.roundToInt

/**
 * Constantes para cálculos de distancia entre dispositivos
 */
object Constants {
    const val CLOSE_DISTANCE_THRESHOLD = 2.0  // 2 metros o menos
    const val MEDIUM_DISTANCE_THRESHOLD = 5.0 // 5 metros o menos
}

@Parcelize
data class NearbyDevice(
    val id: String,
    val name: String,
    val rssi: Int,
    val distance: Double,
    val lastSeen: Long,
    val isConnected: Boolean = false,
    val isFavorite: Boolean = false,
    val userData: UserData? = null,
    val isPaired: Boolean = false
) : Parcelable {

    fun getFormattedDistance(): String {
        return when {
            distance < 1.0 -> "< 1 metro"
            else -> "${distance.roundToInt()} metros"
        }
    }

    fun getDistanceCategory(): DistanceCategory {
        return when {
            distance <= Constants.CLOSE_DISTANCE_THRESHOLD -> DistanceCategory.CLOSE
            distance <= Constants.MEDIUM_DISTANCE_THRESHOLD -> DistanceCategory.MEDIUM
            else -> DistanceCategory.FAR
        }
    }
}

@Parcelize
data class UserData(
    val userId: String,
    val name: String,
    val isProfessional: Boolean
) : Parcelable

enum class DistanceCategory {
    CLOSE, MEDIUM, FAR
}