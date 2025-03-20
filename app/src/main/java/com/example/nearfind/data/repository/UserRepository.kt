package com.example.nearfind.data.repository

import com.example.nearfind.data.model.PairingRequest
import com.example.nearfind.data.model.User
import com.example.nearfind.util.DataStoreManager
import com.example.nearfind.util.UserManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class UserRepository(
    private val dataStoreManager: DataStoreManager,
    private val userManager: UserManager
) {
    // Obtiene el usuario actual como Flow
    fun getCurrentUser(): Flow<User?> = dataStoreManager.getUser()

    // Registra un nuevo usuario
    suspend fun registerUser(firstName: String, lastName: String, isProfessional: Boolean): User {
        return userManager.registerUser(firstName, lastName, isProfessional)
    }

    // Verifica si el usuario está registrado
    fun isUserRegistered(): Flow<Boolean> = dataStoreManager.isUserRegistered()

    // Obtiene las solicitudes de emparejamiento
    fun getPairingRequests(): Flow<List<PairingRequest>> = dataStoreManager.getPairingRequests()

    // Crea una nueva solicitud de emparejamiento
    suspend fun createPairingRequest(receiverId: String, receiverName: String): PairingRequest {
        val currentUser = dataStoreManager.getUser().first() ?: return PairingRequest(
            id = "",
            requesterId = "",
            requesterName = "",
            receiverId = ""
        )

        val request = PairingRequest(
            id = UUID.randomUUID().toString(),
            requesterId = currentUser.id,
            requesterName = "${currentUser.firstName} ${currentUser.lastName}",
            receiverId = receiverId
        )

        userManager.savePairingRequest(request)
        return request
    }

    // Acepta una solicitud de emparejamiento
    suspend fun acceptPairingRequest(requestId: String) {
        userManager.acceptPairingRequest(requestId)
    }

    // Rechaza una solicitud de emparejamiento
    suspend fun rejectPairingRequest(requestId: String) {
        userManager.rejectPairingRequest(requestId)
    }

    // Obtiene la lista de dispositivos emparejados
    fun getPairedDevices(): Flow<List<String>> = dataStoreManager.getPairedDevices()

    // Verifica si un dispositivo está emparejado
    suspend fun isDevicePaired(deviceId: String): Boolean {
        return dataStoreManager.getPairedDevices().first().contains(deviceId)
    }

    // Añade un dispositivo a la lista de emparejados
    suspend fun addPairedDevice(deviceId: String) {
        userManager.addPairedDevice(deviceId)
    }

    // Elimina un dispositivo de la lista de emparejados
    suspend fun removePairedDevice(deviceId: String) {
        userManager.removePairedDevice(deviceId)
    }
}