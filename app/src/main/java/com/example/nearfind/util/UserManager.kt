package com.example.nearfind.util

import com.example.nearfind.data.model.PairingRequest
import com.example.nearfind.data.model.PairingRequestStatus
import com.example.nearfind.data.model.User
import com.google.common.hash.Hashing
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import android.content.Context
import java.nio.charset.StandardCharsets
import java.util.UUID

// Implementación de patrón Singleton
class UserManager private constructor(context: Context) {

    private val dataStoreManager = DataStoreManager(context)

    // Implementación del patrón Singleton
    companion object {
        @Volatile
        private var instance: UserManager? = null

        fun getInstance(context: Context): UserManager {
            return instance ?: synchronized(this) {
                instance ?: UserManager(context).also { instance = it }
            }
        }
    }

    // Verifica si el usuario ya está registrado
    fun isUserRegistered(): Boolean = runBlocking {
        dataStoreManager.isUserRegistered().first()
    }

    // Obtiene el usuario actual
    fun getCurrentUser(): User? = runBlocking {
        dataStoreManager.getUser().first()
    }

    // Registra un nuevo usuario
    suspend fun registerUser(firstName: String, lastName: String, isProfessional: Boolean): User {
        // Generar un ID único basado en el nombre y un UUID
        val rawId = "$firstName$lastName${UUID.randomUUID()}"
        val userId = Hashing.sha256()
            .hashString(rawId, StandardCharsets.UTF_8)
            .toString()
            .take(16) // Tomar solo los primeros 16 caracteres

        val user = User(
            id = userId,
            firstName = firstName,
            lastName = lastName,
            isProfessional = isProfessional
        )

        dataStoreManager.saveUser(user)
        return user
    }

    // Resto de métodos sin cambios...
    suspend fun savePairingRequest(request: PairingRequest) {
        val currentRequests = dataStoreManager.getPairingRequests().first().toMutableList()

        val existingRequestIndex = currentRequests.indexOfFirst {
            it.requesterId == request.requesterId && it.receiverId == request.receiverId
        }

        if (existingRequestIndex != -1) {
            currentRequests[existingRequestIndex] = request
        } else {
            currentRequests.add(request)
        }

        dataStoreManager.savePairingRequests(currentRequests)
    }

    fun getPendingRequests(): List<PairingRequest> = runBlocking {
        dataStoreManager.getPairingRequests().first().filter {
            it.status == PairingRequestStatus.PENDING && it.receiverId == getCurrentUser()?.id
        }
    }

    suspend fun acceptPairingRequest(requestId: String) {
        val currentRequests = dataStoreManager.getPairingRequests().first().toMutableList()
        val requestIndex = currentRequests.indexOfFirst { it.id == requestId }

        if (requestIndex != -1) {
            val updatedRequest = currentRequests[requestIndex].copy(status = PairingRequestStatus.ACCEPTED)
            currentRequests[requestIndex] = updatedRequest
            dataStoreManager.savePairingRequests(currentRequests)

            val currentApprovedUsers = dataStoreManager.getApprovedUsers().first().toMutableList()
            currentApprovedUsers.add(updatedRequest.requesterId)
            dataStoreManager.saveApprovedUsers(currentApprovedUsers)
        }
    }

    suspend fun rejectPairingRequest(requestId: String) {
        val currentRequests = dataStoreManager.getPairingRequests().first().toMutableList()
        val requestIndex = currentRequests.indexOfFirst { it.id == requestId }

        if (requestIndex != -1) {
            val updatedRequest = currentRequests[requestIndex].copy(status = PairingRequestStatus.REJECTED)
            currentRequests[requestIndex] = updatedRequest
            dataStoreManager.savePairingRequests(currentRequests)
        }
    }

    fun isUserApproved(userId: String): Boolean = runBlocking {
        dataStoreManager.getApprovedUsers().first().contains(userId)
    }

    fun getPairedDevices(): List<String> = runBlocking {
        dataStoreManager.getPairedDevices().first()
    }

    suspend fun addPairedDevice(deviceId: String) {
        val currentPairedDevices = dataStoreManager.getPairedDevices().first().toMutableList()
        if (!currentPairedDevices.contains(deviceId)) {
            currentPairedDevices.add(deviceId)
            dataStoreManager.savePairedDevices(currentPairedDevices)
        }
    }

    suspend fun removePairedDevice(deviceId: String) {
        val currentPairedDevices = dataStoreManager.getPairedDevices().first().toMutableList()
        currentPairedDevices.remove(deviceId)
        dataStoreManager.savePairedDevices(currentPairedDevices)
    }
}