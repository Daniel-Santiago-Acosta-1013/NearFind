package com.example.nearfind.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nearfind.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _firstName = MutableStateFlow("")
    val firstName: StateFlow<String> = _firstName.asStateFlow()

    private val _lastName = MutableStateFlow("")
    val lastName: StateFlow<String> = _lastName.asStateFlow()

    private val _isProfessional = MutableStateFlow(false)
    val isProfessional: StateFlow<Boolean> = _isProfessional.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _registrationComplete = MutableStateFlow(false)
    val registrationComplete: StateFlow<Boolean> = _registrationComplete.asStateFlow()

    fun setFirstName(name: String) {
        _firstName.value = name
    }

    fun setLastName(name: String) {
        _lastName.value = name
    }

    fun setProfessional(isProfessional: Boolean) {
        _isProfessional.value = isProfessional
    }

    fun register() {
        if (firstName.value.isBlank() || lastName.value.isBlank()) {
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                userRepository.registerUser(
                    firstName = firstName.value,
                    lastName = lastName.value,
                    isProfessional = isProfessional.value
                )
                _registrationComplete.value = true
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}