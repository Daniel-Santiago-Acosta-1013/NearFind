package com.example.nearfind.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nearfind.data.model.ConnectionState

@Composable
fun ConnectionButton(
    connectionState: ConnectionState,
    isConnected: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    when (connectionState) {
        ConnectionState.CONNECTING, ConnectionState.DISCONNECTING -> {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(24.dp)
                    .height(24.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp
            )
        }
        else -> {
            Button(
                onClick = {
                    if (isConnected) {
                        onDisconnect()
                    } else {
                        onConnect()
                    }
                },
                colors = if (isConnected) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Text(
                    text = if (isConnected) "Desconectar" else "Conectar"
                )
            }
        }
    }
}