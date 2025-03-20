package com.example.nearfind.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nearfind.data.model.DistanceCategory
import com.example.nearfind.data.model.NearbyDevice
import com.example.nearfind.data.model.PairingRequestStatus
import com.example.nearfind.data.model.User
import com.example.nearfind.data.repository.BluetoothRepository
import com.example.nearfind.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val bluetoothRepository: BluetoothRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val isScanning: StateFlow<Boolean> = bluetoothRepository.isScanningFlow

    // Información del usuario actual
    val userInfo: StateFlow<User> = userRepository.getCurrentUser()
        .map { it ?: User("", "", "", false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = User("", "", "", false)
        )

    // Contador de solicitudes de emparejamiento pendientes
    val pendingRequestsCount: StateFlow<Int> = userRepository.getPairingRequests()
        .map { requests ->
            requests.count {
                it.status == PairingRequestStatus.PENDING && it.receiverId == userInfo.value.id
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val nearbyDevices: StateFlow<List<NearbyDevice>> = combine(
        bluetoothRepository.nearbyDevicesFlow,
        searchQuery
    ) { devices, query ->
        if (query.isBlank()) {
            devices
        } else {
            devices.filter { device ->
                device.name.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val closeDevices: StateFlow<List<NearbyDevice>> = nearbyDevices.combine(nearbyDevices) { devices, _ ->
        devices.filter { it.getDistanceCategory() == DistanceCategory.CLOSE }
            .sortedBy { it.distance }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleScan() {
        if (isScanning.value) {
            bluetoothRepository.stopScan()
        } else {
            bluetoothRepository.startScan()
        }
    }

    // Verificar si hay solicitudes pendientes
    fun checkPendingRequests() {
        viewModelScope.launch {
            val requests = userRepository.getPairingRequests().first()
            val userId = userRepository.getCurrentUser().first()?.id ?: return@launch

            val pendingCount = requests.count {
                it.status == PairingRequestStatus.PENDING && it.receiverId == userId
            }
        }
    }
}