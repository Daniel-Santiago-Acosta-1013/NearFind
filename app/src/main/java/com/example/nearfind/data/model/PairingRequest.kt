package com.example.nearfind.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PairingRequest(
    val id: String,
    val requesterId: String,
    val requesterName: String,
    val receiverId: String,
    val status: PairingRequestStatus = PairingRequestStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable

enum class PairingRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}