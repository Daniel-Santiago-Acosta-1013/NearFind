package com.example.nearfind.ui.screens.devicedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nearfind.data.model.ConnectionState
import com.example.nearfind.data.model.NearbyDevice
import com.example.nearfind.data.repository.BluetoothRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DeviceDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val bluetoothRepository: BluetoothRepository
) : ViewModel() {

    private val deviceId: String = savedStateHandle["deviceId"] ?: ""

    private val _device = MutableStateFlow<NearbyDevice?>(null)
    val device = _device.asStateFlow()

    val connectionState: StateFlow<ConnectionState> = bluetoothRepository.connectionStateFlow

    val isConnectedToThisDevice: StateFlow<Boolean> = bluetoothRepository.connectedDeviceIdFlow.map { id ->
        id == deviceId
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    init {
        loadDevice()
    }

    private fun loadDevice() {
        viewModelScope.launch {
            _device.value = bluetoothRepository.getDeviceById(deviceId)
        }
    }

    fun connectToDevice() {
        bluetoothRepository.connectToDevice(deviceId)
    }

    fun disconnectFromDevice() {
        bluetoothRepository.disconnectFromDevice()
    }
}