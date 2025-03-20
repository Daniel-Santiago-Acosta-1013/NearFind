package com.example.nearfind.ui.screens.pairing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nearfind.bluetooth.PairingState
import com.example.nearfind.data.model.NearbyDevice
import com.example.nearfind.di.ViewModelFactory
import com.example.nearfind.ui.components.DeviceCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingScreen(
    navigateBack: () -> Unit,
    isBluetoothEnabled: Boolean,
    requestBluetoothEnable: () -> Unit,
    viewModel: PairingViewModel = viewModel(factory = ViewModelFactory.Factory)
) {
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val devices by viewModel.nearbyDevices.collectAsStateWithLifecycle()
    val pairedDevices by viewModel.pairedDevices.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val pairingState by viewModel.pairingState.collectAsStateWithLifecycle()

    var selectedDevice by remember { mutableStateOf<NearbyDevice?>(null) }
    var showUnpairDialog by remember { mutableStateOf(false) }
    var deviceToUnpair by remember { mutableStateOf<NearbyDevice?>(null) }

    LaunchedEffect(key1 = true) {
        // Iniciar escaneo automáticamente si Bluetooth está habilitado
        if (!isScanning && isBluetoothEnabled) {
            viewModel.toggleScan()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emparejamiento") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            if (isBluetoothEnabled) {
                FloatingActionButton(onClick = { viewModel.toggleScan() }) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Iniciar escaneo"
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        // Verificar si el Bluetooth está habilitado
        if (!isBluetoothEnabled) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = "Bluetooth",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Bluetooth desactivado",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Es necesario activar el Bluetooth para buscar dispositivos",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = requestBluetoothEnable
                ) {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Activar Bluetooth")
                }
            }
        } else {
            // Contenido normal cuando el Bluetooth está activado
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar dispositivos") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (pairedDevices.isNotEmpty()) {
                    Text(
                        text = "Dispositivos emparejados",
                        style = MaterialTheme.typography.titleMedium
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(pairedDevices) { device ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    deviceToUnpair = device
                                    showUnpairDialog = true
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = device.name,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = device.userData?.name ?: "Usuario desconocido",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    IconButton(onClick = {
                                        deviceToUnpair = device
                                        showUnpairDialog = true
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Desemparejar",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = "Dispositivos disponibles",
                    style = MaterialTheme.typography.titleMedium
                )

                if (devices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isScanning) {
                                "Buscando dispositivos..."
                            } else {
                                "No se encontraron dispositivos"
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(devices) { device ->
                            DeviceCard(
                                device = device,
                                onClick = {
                                    selectedDevice = device
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo de solicitud de emparejamiento
    if (selectedDevice != null) {
        AlertDialog(
            onDismissRequest = {
                if (pairingState !is PairingState.Connecting &&
                    pairingState !is PairingState.SendingRequest) {
                    selectedDevice = null
                    viewModel.resetPairingState()
                }
            },
            title = { Text("Solicitud de emparejamiento") },
            text = {
                Column {
                    Text("¿Deseas enviar una solicitud de emparejamiento a ${selectedDevice?.name}?")

                    Spacer(modifier = Modifier.height(16.dp))

                    when (pairingState) {
                        is PairingState.Connecting,
                        is PairingState.SendingRequest -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.width(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = if (pairingState is PairingState.Connecting) {
                                        "Conectando..."
                                    } else {
                                        "Enviando solicitud..."
                                    }
                                )
                            }
                        }
                        is PairingState.Error -> {
                            Text(
                                text = (pairingState as PairingState.Error).message,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        is PairingState.Success -> {
                            Text(
                                text = "Solicitud enviada con éxito",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        else -> {
                            // Idle state - no additional content
                        }
                    }
                }
            },
            confirmButton = {
                if (pairingState !is PairingState.Connecting &&
                    pairingState !is PairingState.SendingRequest) {
                    Button(
                        onClick = {
                            if (pairingState is PairingState.Success) {
                                selectedDevice = null
                                viewModel.resetPairingState()
                            } else {
                                selectedDevice?.let { viewModel.sendPairingRequest(it) }
                            }
                        }
                    ) {
                        Text(
                            if (pairingState is PairingState.Success) "Cerrar" else "Enviar solicitud"
                        )
                    }
                }
            },
            dismissButton = {
                if (pairingState !is PairingState.Connecting &&
                    pairingState !is PairingState.SendingRequest) {
                    TextButton(
                        onClick = {
                            selectedDevice = null
                            viewModel.resetPairingState()
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        )
    }

    // Diálogo para desemparejar
    if (showUnpairDialog && deviceToUnpair != null) {
        AlertDialog(
            onDismissRequest = {
                showUnpairDialog = false
                deviceToUnpair = null
            },
            title = { Text("Desemparejar dispositivo") },
            text = { Text("¿Estás seguro de que deseas desemparejar de ${deviceToUnpair?.name}?") },
            confirmButton = {
                Button(
                    onClick = {
                        deviceToUnpair?.let { viewModel.unpairDevice(it.id) }
                        showUnpairDialog = false
                        deviceToUnpair = null
                    }
                ) {
                    Text("Desemparejar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showUnpairDialog = false
                        deviceToUnpair = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}