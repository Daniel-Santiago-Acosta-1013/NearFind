package com.example.nearfind.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nearfind.di.ViewModelFactory
import com.example.nearfind.ui.components.DeviceCard
import com.example.nearfind.util.PermissionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToDeviceDetail: (String) -> Unit,
    navigateToSettings: () -> Unit,
    navigateToPairing: () -> Unit,
    navigateToPairingRequests: () -> Unit,
    permissionManager: PermissionManager,
    startScanService: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = ViewModelFactory.Factory)
) {
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val devices by viewModel.nearbyDevices.collectAsStateWithLifecycle()
    val closeDevices by viewModel.closeDevices.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val pendingRequestsCount by viewModel.pendingRequestsCount.collectAsStateWithLifecycle()
    val userInfo by viewModel.userInfo.collectAsStateWithLifecycle()

    // Evitamos llamar directamente a RequestPermissions
    // En su lugar, iniciamos el servicio directamente
    LaunchedEffect(Unit) {
        startScanService()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NearFind") },
                actions = {
                    if (userInfo.isProfessional) {
                        // Notification icon with custom badge
                        Box {
                            IconButton(onClick = navigateToPairingRequests) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Solicitudes de emparejamiento"
                                )
                            }

                            // Custom badge
                            if (pendingRequestsCount > 0) {
                                Surface(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .align(Alignment.TopEnd),
                                    color = MaterialTheme.colorScheme.error,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = pendingRequestsCount.toString(),
                                        color = MaterialTheme.colorScheme.onError,
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(1.dp)
                                    )
                                }
                            }
                        }
                    }

                    IconButton(onClick = navigateToPairing) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Emparejamiento"
                        )
                    }

                    IconButton(onClick = navigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Configuración"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Mostrar información del usuario
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Perfil: ${userInfo.getFullName()}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = if (userInfo.isProfessional) "Tipo: Especialista" else "Tipo: Usuario regular",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "ID: ${userInfo.id}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar dispositivos") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (closeDevices.isNotEmpty()) {
                Text(
                    text = "Dispositivos cercanos",
                    style = MaterialTheme.typography.titleMedium
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(closeDevices) { device ->
                        DeviceCard(
                            device = device,
                            onClick = { navigateToDeviceDetail(device.id) }
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp))
            }

            Text(
                text = "Todos los dispositivos",
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
                            onClick = { navigateToDeviceDetail(device.id) }
                        )
                    }
                }
            }
        }
    }
}