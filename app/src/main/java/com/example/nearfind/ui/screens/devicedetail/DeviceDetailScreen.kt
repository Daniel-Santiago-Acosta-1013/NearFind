package com.example.nearfind.ui.screens.devicedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nearfind.data.model.ConnectionState
import com.example.nearfind.di.ViewModelFactory
import com.example.nearfind.ui.components.DistanceDisplay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    deviceId: String,
    navigateBack: () -> Unit,
    viewModel: DeviceDetailViewModel = viewModel(factory = ViewModelFactory.Factory)
) {
    val device by viewModel.device.collectAsStateWithLifecycle()
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val isConnected by viewModel.isConnectedToThisDevice.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Detalles del dispositivo") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (device == null) {
                Text(
                    text = "Dispositivo no encontrado",
                    style = MaterialTheme.typography.bodyLarge
                )
                return@Scaffold
            }

            device?.let { device ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "ID: ${device.id}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        if (device.userData != null) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Usuario: ${device.userData.name}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = if (device.userData.isProfessional) "Especialista" else "Usuario regular",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Intensidad señal",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${device.rssi} dBm",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            DistanceDisplay(
                                distance = device.distance,
                                category = device.getDistanceCategory()
                            )
                        }

                        if (device.isPaired) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Dispositivo emparejado",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Green
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Estado de conexión",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                val connectionStateText = when (connectionState) {
                    ConnectionState.CONNECTED -> "Conectado"
                    ConnectionState.CONNECTING -> "Conectando..."
                    ConnectionState.DISCONNECTING -> "Desconectando..."
                    ConnectionState.DISCONNECTED -> "Desconectado"
                }

                Text(
                    text = connectionStateText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (connectionState == ConnectionState.CONNECTED) Color.Green else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (connectionState == ConnectionState.CONNECTING || connectionState == ConnectionState.DISCONNECTING) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            if (isConnected) {
                                viewModel.disconnectFromDevice()
                            } else {
                                viewModel.connectToDevice()
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
                        Text(text = if (isConnected) "Desconectar" else "Conectar")
                    }
                }
            }
        }
    }
}