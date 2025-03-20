package com.example.nearfind.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.nearfind.ui.screens.home.HomeScreen
import com.example.nearfind.ui.screens.devicedetail.DeviceDetailScreen
import com.example.nearfind.ui.screens.settings.SettingsScreen
import com.example.nearfind.ui.screens.onboarding.OnboardingScreen
import com.example.nearfind.ui.screens.pairing.PairingScreen
import com.example.nearfind.ui.screens.pairing.PairingRequestsScreen
import com.example.nearfind.util.PermissionManager

@Composable
fun NavGraph(
    navController: NavHostController,
    permissionManager: PermissionManager,
    startScanService: () -> Unit,
    isUserRegistered: Boolean,
    isBluetoothEnabled: Boolean,
    startDestination: String,
    requestBluetoothEnable: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screen.Onboarding.route) {
            OnboardingScreen(
                onRegistrationComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Home.route) {
            HomeScreen(
                navigateToDeviceDetail = { deviceId ->
                    navController.navigate(Screen.DeviceDetail.createRoute(deviceId))
                },
                navigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                navigateToPairing = {
                    navController.navigate(Screen.Pairing.route)
                },
                navigateToPairingRequests = {
                    navController.navigate(Screen.PairingRequests.route)
                },
                permissionManager = permissionManager,
                startScanService = startScanService,
                isBluetoothEnabled = isBluetoothEnabled,
                requestBluetoothEnable = requestBluetoothEnable
            )
        }

        composable(
            route = Screen.DeviceDetail.route,
            arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
            DeviceDetailScreen(
                deviceId = deviceId,
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen(
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.Pairing.route) {
            PairingScreen(
                navigateBack = {
                    navController.popBackStack()
                },
                isBluetoothEnabled = isBluetoothEnabled,
                requestBluetoothEnable = requestBluetoothEnable
            )
        }

        composable(route = Screen.PairingRequests.route) {
            PairingRequestsScreen(
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Settings : Screen("settings")
    object DeviceDetail : Screen("device/{deviceId}") {
        fun createRoute(deviceId: String) = "device/$deviceId"
    }
    object Pairing : Screen("pairing")
    object PairingRequests : Screen("pairing_requests")
}