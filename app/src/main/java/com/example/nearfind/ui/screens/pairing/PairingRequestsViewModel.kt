package com.example.nearfind.ui.screens.pairing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nearfind.data.model.PairingRequest
import com.example.nearfind.data.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PairingRequestsViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    val pendingRequests: StateFlow<List<PairingRequest>> = userRepository.getPairingRequests()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun acceptRequest(requestId: String) {
        viewModelScope.launch {
            userRepository.acceptPairingRequest(requestId)
        }
    }

    fun rejectRequest(requestId: String) {
        viewModelScope.launch {
            userRepository.rejectPairingRequest(requestId)
        }
    }
}