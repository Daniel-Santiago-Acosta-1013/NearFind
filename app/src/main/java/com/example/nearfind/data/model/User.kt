package com.example.nearfind.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val isProfessional: Boolean = false,
    val isVerified: Boolean = false
) : Parcelable {
    fun getFullName(): String = "$firstName $lastName"
}