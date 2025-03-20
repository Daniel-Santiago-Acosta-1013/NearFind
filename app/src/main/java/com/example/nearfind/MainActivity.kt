package com.example.nearfind

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.nearfind.service.BluetoothScanService
import com.example.nearfind.ui.navigation.NavGraph
import com.example.nearfind.ui.theme.NearFindTheme
import com.example.nearfind.util.PermissionManager
import com.example.nearfind.util.UserManager

class MainActivity : ComponentActivity() {

    // Obtenemos la instancia de UserManager a través del AppContainer
    private val userManager by lazy {
        (application as NearFindApplication).appContainer.userManager
    }

    private val requiredPermissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.FOREGROUND_SERVICE_LOCATION
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            add(Manifest.permission.BLUETOOTH)
            add(Manifest.permission.BLUETOOTH_ADMIN)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NearFindTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        permissionManager = PermissionManager(this, requiredPermissions),
                        startScanService = { startBluetoothScanService() },
                        isUserRegistered = userManager.isUserRegistered(),
                        startDestination = if (userManager.isUserRegistered()) "home" else "onboarding"
                    )
                }
            }
        }
    }

    private fun startBluetoothScanService() {
        val serviceIntent = Intent(this, BluetoothScanService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}