package com.example.nearfind.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.nearfind.data.model.PairingRequest
import com.example.nearfind.data.model.PairingRequestStatus
import com.example.nearfind.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

// Extensión para crear un DataStore único por aplicación
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "near_find_preferences")

class DataStoreManager(context: Context) {
    private val dataStore = context.dataStore

    // Keys para las preferencias
    companion object {
        val USER_DATA_KEY = stringPreferencesKey("user_data")
        val IS_USER_REGISTERED_KEY = booleanPreferencesKey("is_user_registered")
        val PAIRED_DEVICES_KEY = stringPreferencesKey("paired_devices")
        val PAIRING_REQUESTS_KEY = stringPreferencesKey("pairing_requests")
        val APPROVED_USERS_KEY = stringPreferencesKey("approved_users")
    }

    // Implementación de serialización/deserialización con JSONObject
    private fun userToJson(user: User): String {
        val json = JSONObject()
        json.put("id", user.id)
        json.put("firstName", user.firstName)
        json.put("lastName", user.lastName)
        json.put("isProfessional", user.isProfessional)
        return json.toString()
    }

    private fun jsonToUser(json: String): User? {
        return try {
            val jsonObject = JSONObject(json)
            User(
                id = jsonObject.getString("id"),
                firstName = jsonObject.getString("firstName"),
                lastName = jsonObject.getString("lastName"),
                isProfessional = jsonObject.getBoolean("isProfessional")
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun listToJsonString(list: List<String>): String {
        val jsonArray = JSONArray()
        list.forEach { jsonArray.put(it) }
        return jsonArray.toString()
    }

    private fun jsonStringToStringList(json: String): List<String> {
        return try {
            val jsonArray = JSONArray(json)
            List(jsonArray.length()) { jsonArray.getString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun pairingRequestsToJson(requests: List<PairingRequest>): String {
        val jsonArray = JSONArray()
        requests.forEach { request ->
            val jsonObject = JSONObject()
            jsonObject.put("id", request.id)
            jsonObject.put("requesterId", request.requesterId)
            jsonObject.put("requesterName", request.requesterName)
            jsonObject.put("receiverId", request.receiverId)
            jsonObject.put("status", request.status.name)
            jsonObject.put("timestamp", request.timestamp)
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    private fun jsonToPairingRequests(json: String): List<PairingRequest> {
        return try {
            val jsonArray = JSONArray(json)
            List(jsonArray.length()) { index ->
                val jsonObject = jsonArray.getJSONObject(index)
                PairingRequest(
                    id = jsonObject.getString("id"),
                    requesterId = jsonObject.getString("requesterId"),
                    requesterName = jsonObject.getString("requesterName"),
                    receiverId = jsonObject.getString("receiverId"),
                    status = PairingRequestStatus.valueOf(jsonObject.getString("status")),
                    timestamp = jsonObject.getLong("timestamp")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // El resto del código permanece igual...

    // Guardar usuario
    suspend fun saveUser(user: User) {
        val userJson = userToJson(user)
        dataStore.edit { preferences ->
            preferences[USER_DATA_KEY] = userJson
            preferences[IS_USER_REGISTERED_KEY] = true
        }
    }

    // Obtener usuario
    fun getUser(): Flow<User?> = dataStore.data.map { preferences ->
        val userJson = preferences[USER_DATA_KEY]
        if (userJson != null) {
            jsonToUser(userJson)
        } else {
            null
        }
    }

    // Verificar si el usuario está registrado
    fun isUserRegistered(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_USER_REGISTERED_KEY] ?: false
    }

    // Guardar dispositivos emparejados (IDs)
    suspend fun savePairedDevices(deviceIds: List<String>) {
        val devicesJson = listToJsonString(deviceIds)
        dataStore.edit { preferences ->
            preferences[PAIRED_DEVICES_KEY] = devicesJson
        }
    }

    // Obtener dispositivos emparejados
    fun getPairedDevices(): Flow<List<String>> = dataStore.data.map { preferences ->
        val devicesJson = preferences[PAIRED_DEVICES_KEY]
        if (devicesJson != null) {
            jsonStringToStringList(devicesJson)
        } else {
            emptyList()
        }
    }

    // Guardar solicitudes de emparejamiento
    suspend fun savePairingRequests(requests: List<PairingRequest>) {
        val requestsJson = pairingRequestsToJson(requests)
        dataStore.edit { preferences ->
            preferences[PAIRING_REQUESTS_KEY] = requestsJson
        }
    }

    // Obtener solicitudes de emparejamiento
    fun getPairingRequests(): Flow<List<PairingRequest>> = dataStore.data.map { preferences ->
        val requestsJson = preferences[PAIRING_REQUESTS_KEY]
        if (requestsJson != null) {
            jsonToPairingRequests(requestsJson)
        } else {
            emptyList()
        }
    }

    // Guardar usuarios aprobados (para profesionales)
    suspend fun saveApprovedUsers(userIds: List<String>) {
        val usersJson = listToJsonString(userIds)
        dataStore.edit { preferences ->
            preferences[APPROVED_USERS_KEY] = usersJson
        }
    }

    // Obtener usuarios aprobados
    fun getApprovedUsers(): Flow<List<String>> = dataStore.data.map { preferences ->
        val usersJson = preferences[APPROVED_USERS_KEY]
        if (usersJson != null) {
            jsonStringToStringList(usersJson)
        } else {
            emptyList()
        }
    }
}