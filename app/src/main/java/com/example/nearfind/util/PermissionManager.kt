package com.example.nearfind.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat

class PermissionManager(
    private val context: Context,
    private val permissions: Array<String>
) {
    fun hasPermissions(): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    @Composable
    fun RequestPermissions(
        onPermissionsGranted: () -> Unit,
        onPermissionsDenied: () -> Unit
    ) {
        var permissionsGranted by remember { mutableStateOf(hasPermissions()) }

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionsResult ->
            permissionsGranted = permissionsResult.values.all { it }
            if (permissionsGranted) {
                onPermissionsGranted()
            } else {
                onPermissionsDenied()
            }
        }

        LaunchedEffect(key1 = permissionsGranted) {
            if (!permissionsGranted) {
                permissionLauncher.launch(permissions)
            } else {
                onPermissionsGranted()
            }
        }
    }
}