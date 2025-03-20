package com.example.nearfind.ui.screens.pairing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nearfind.bluetooth.PairingManager
import com.example.nearfind.bluetooth.PairingState
import com.example.nearfind.data.model.NearbyDevice
import com.example.nearfind.data.repository.BluetoothRepository
import com.example.nearfind.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PairingViewModel(
    private val bluetoothRepository: BluetoothRepository,
    private val userRepository: UserRepository,
    private val pairingManager: PairingManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val isScanning: StateFlow<Boolean> = bluetoothRepository.isScanningFlow

    val pairingState: StateFlow<PairingState> = pairingManager.pairingState

    val nearbyDevices: StateFlow<List<NearbyDevice>> = combine(
        bluetoothRepository.nearbyDevicesFlow,
        userRepository.getPairedDevices(),
        searchQuery
    ) { devices, pairedDevices, query ->
        devices.filter { device ->
            (query.isBlank() || device.name.contains(query, ignoreCase = true)) &&
                    !pairedDevices.contains(device.id)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val pairedDevices: StateFlow<List<NearbyDevice>> = combine(
        bluetoothRepository.nearbyDevicesFlow,
        userRepository.getPairedDevices()
    ) { devices, pairedDevices ->
        devices.filter { device -> pairedDevices.contains(device.id) }
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

    fun sendPairingRequest(device: NearbyDevice) {
        pairingManager.sendPairingRequest(device)
    }

    fun resetPairingState() {
        pairingManager.resetPairingState()
    }

    fun unpairDevice(deviceId: String) {
        viewModelScope.launch {
            userRepository.removePairedDevice(deviceId)
        }
    }
}