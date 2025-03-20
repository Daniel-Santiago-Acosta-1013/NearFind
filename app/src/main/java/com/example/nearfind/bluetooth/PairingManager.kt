package com.example.nearfind.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.nearfind.data.model.NearbyDevice
import com.example.nearfind.data.model.PairingRequest
import com.example.nearfind.data.model.UserData
import com.example.nearfind.data.repository.UserRepository
import com.example.nearfind.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

class PairingManager(
    private val context: Context,
    private val bleConnectionManager: BleConnectionManager,
    private val userRepository: UserRepository
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _pairingState = MutableStateFlow<PairingState>(PairingState.Idle)
    val pairingState: StateFlow<PairingState> = _pairingState.asStateFlow()

    fun sendPairingRequest(targetDevice: NearbyDevice) {
        if (!hasRequiredPermissions()) {
            _pairingState.value = PairingState.Error("No se tienen los permisos necesarios")
            return
        }

        sendPairingRequestWithPermissionCheck(targetDevice)
    }

    @SuppressLint("MissingPermission")
    private fun sendPairingRequestWithPermissionCheck(targetDevice: NearbyDevice) {
        coroutineScope.launch {
            try {
                _pairingState.value = PairingState.Connecting

                // Conectar al dispositivo
                val connected = bleConnectionManager.connect(targetDevice.id)
                if (!connected) {
                    _pairingState.value = PairingState.Error("No se pudo conectar al dispositivo")
                    return@launch
                }

                _pairingState.value = PairingState.SendingRequest

                // Obtener el usuario actual
                val currentUser = userRepository.getCurrentUser().first()
                if (currentUser == null) {
                    _pairingState.value = PairingState.Error("No hay usuario registrado")
                    bleConnectionManager.disconnect()
                    return@launch
                }

                // Crear los datos del usuario para enviar
                val userData = UserData(
                    userId = currentUser.id,
                    name = "${currentUser.firstName} ${currentUser.lastName}",
                    isProfessional = currentUser.isProfessional
                )

                // Serializar los datos manualmente con JSONObject
                val userDataJson = serializeUserData(userData)

                // Obtener el servicio y característica de emparejamiento
                val service = bleConnectionManager.getService(UUID.fromString(Constants.PAIRING_SERVICE_UUID))
                if (service == null) {
                    _pairingState.value = PairingState.Error("El dispositivo no soporta el servicio de emparejamiento")
                    bleConnectionManager.disconnect()
                    return@launch
                }

                val characteristic = service.getCharacteristic(UUID.fromString(Constants.PAIRING_CHARACTERISTIC_UUID))
                if (characteristic == null) {
                    _pairingState.value = PairingState.Error("El dispositivo no soporta la característica de emparejamiento")
                    bleConnectionManager.disconnect()
                    return@launch
                }

                // Enviar los datos
                val success = bleConnectionManager.writeCharacteristic(
                    characteristic,
                    userDataJson.toByteArray(Charsets.UTF_8)
                )

                if (!success) {
                    _pairingState.value = PairingState.Error("Error al enviar la solicitud de emparejamiento")
                    bleConnectionManager.disconnect()
                    return@launch
                }

                // Crear y guardar la solicitud de emparejamiento localmente
                val request = userRepository.createPairingRequest(
                    receiverId = targetDevice.userData?.userId ?: targetDevice.id,
                    receiverName = targetDevice.userData?.name ?: targetDevice.name
                )

                _pairingState.value = PairingState.Success

                // Desconectar después de enviar la solicitud
                bleConnectionManager.disconnect()

            } catch (e: Exception) {
                Log.e("PairingManager", "Error during pairing process", e)
                _pairingState.value = PairingState.Error("Error: ${e.message}")
                bleConnectionManager.disconnect()
            }
        }
    }

    fun resetPairingState() {
        _pairingState.value = PairingState.Idle
    }

    fun processPairingRequest(userData: UserData) {
        coroutineScope.launch {
            // Crear y guardar la solicitud de emparejamiento
            val currentUser = userRepository.getCurrentUser().first()
            if (currentUser != null) {
                val pairingRequest = PairingRequest(
                    id = UUID.randomUUID().toString(),
                    requesterId = userData.userId,
                    requesterName = userData.name,
                    receiverId = currentUser.id
                )

                userRepository.createPairingRequest(
                    receiverId = userData.userId,
                    receiverName = userData.name
                )
            }
        }
    }

    // Serializador simple para reemplazar Moshi
    private fun serializeUserData(userData: UserData): String {
        val json = JSONObject()
        json.put("userId", userData.userId)
        json.put("name", userData.name)
        json.put("isProfessional", userData.isProfessional)
        return json.toString()
    }

    // Deserializador simple para reemplazar Moshi
    fun deserializeUserData(json: String): UserData {
        val jsonObject = JSONObject(json)
        return UserData(
            userId = jsonObject.getString("userId"),
            name = jsonObject.getString("name"),
            isProfessional = jsonObject.getBoolean("isProfessional")
        )
    }

    private fun hasRequiredPermissions(): Boolean {
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }

        return requiredPermissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        private var instance: PairingManager? = null

        fun getInstance(
            context: Context,
            bleConnectionManager: BleConnectionManager,
            userRepository: UserRepository
        ): PairingManager {
            return instance ?: synchronized(this) {
                instance ?: PairingManager(
                    context.applicationContext,
                    bleConnectionManager,
                    userRepository
                ).also { instance = it }
            }
        }
    }
}

sealed class PairingState {
    object Idle : PairingState()
    object Connecting : PairingState()
    object SendingRequest : PairingState()
    object Success : PairingState()
    data class Error(val message: String) : PairingState()
}