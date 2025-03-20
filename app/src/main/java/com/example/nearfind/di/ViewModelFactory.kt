package com.example.nearfind.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.createSavedStateHandle
import com.example.nearfind.NearFindApplication
import com.example.nearfind.ui.screens.devicedetail.DeviceDetailViewModel
import com.example.nearfind.ui.screens.home.HomeViewModel
import com.example.nearfind.ui.screens.onboarding.OnboardingViewModel
import com.example.nearfind.ui.screens.pairing.PairingRequestsViewModel
import com.example.nearfind.ui.screens.pairing.PairingViewModel
import com.example.nearfind.ui.screens.settings.SettingsViewModel

object ViewModelFactory {
    val Factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as NearFindApplication
            val container = application.appContainer

            return when {
                modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                    SettingsViewModel() as T
                }

                modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                    HomeViewModel(
                        bluetoothRepository = container.bluetoothRepository,
                        userRepository = container.userRepository
                    ) as T
                }

                modelClass.isAssignableFrom(PairingViewModel::class.java) -> {
                    PairingViewModel(
                        bluetoothRepository = container.bluetoothRepository,
                        userRepository = container.userRepository,
                        pairingManager = container.pairingManager
                    ) as T
                }

                modelClass.isAssignableFrom(OnboardingViewModel::class.java) -> {
                    OnboardingViewModel(
                        userRepository = container.userRepository
                    ) as T
                }

                modelClass.isAssignableFrom(DeviceDetailViewModel::class.java) -> {
                    DeviceDetailViewModel(
                        savedStateHandle = extras.createSavedStateHandle(),
                        bluetoothRepository = container.bluetoothRepository
                    ) as T
                }

                modelClass.isAssignableFrom(PairingRequestsViewModel::class.java) -> {
                    PairingRequestsViewModel(
                        userRepository = container.userRepository
                    ) as T
                }

                else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}