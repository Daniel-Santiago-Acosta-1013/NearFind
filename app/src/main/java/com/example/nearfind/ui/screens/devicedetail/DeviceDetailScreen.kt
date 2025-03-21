package com.example.nearfind.ui.screens.devicedetail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SignalCellular0Bar
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.outlined.SignalCellularAlt
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nearfind.data.model.ConnectionState
import com.example.nearfind.data.model.DistanceCategory
import com.example.nearfind.di.ViewModelFactory
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

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

    // Animación para el indicador de conexión
    val connectionProgress by animateFloatAsState(
        targetValue = when (connectionState) {
            ConnectionState.CONNECTED -> 1f
            ConnectionState.CONNECTING -> 0.7f
            ConnectionState.DISCONNECTING -> 0.3f
            ConnectionState.DISCONNECTED -> 0f
        },
        animationSpec = tween(500),
        label = "connectionProgress"
    )

    // Gradiente para el fondo
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.background
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Detalles del dispositivo") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Atrás",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (device == null) {
                    // Estado cuando el dispositivo no se encuentra
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeviceUnknown,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Dispositivo no encontrado",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "El dispositivo con ID: $deviceId no está disponible",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = navigateBack,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Volver")
                        }
                    }

                    return@Scaffold
                }

                device?.let { device ->
                    // Cabecera con avatar e información principal
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.elevatedCardElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar del dispositivo
                            val deviceIcon = when {
                                device.isPaired -> Icons.Default.BluetoothConnected
                                device.name.contains("Android", ignoreCase = true) -> Icons.Default.SmartToy
                                else -> Icons.Default.Bluetooth
                            }

                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isConnected || device.isPaired)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = deviceIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = if (isConnected || device.isPaired)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Nombre del dispositivo
                            Text(
                                text = device.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            // ID del dispositivo
                            Text(
                                text = device.id,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            if (device.isPaired) {
                                Spacer(modifier = Modifier.height(12.dp))

                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.BluetoothConnected,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )

                                        Spacer(modifier = Modifier.width(4.dp))

                                        Text(
                                            "EMPAREJADO",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Información del usuario si está disponible
                    if (device.userData != null) {
                        InfoCard(
                            title = "Información de Usuario",
                            icon = Icons.Default.Person
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = device.userData.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Text(
                                        text = "ID: ${device.userData.userId.take(8)}...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (device.userData.isProfessional) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.tertiary
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.StarRate,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )

                                            Spacer(modifier = Modifier.width(4.dp))

                                            Text(
                                                "PROFESIONAL",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Información de la señal
                    InfoCard(
                        title = "Información de Señal",
                        icon = Icons.Outlined.SignalCellularAlt
                    ) {
                        // Grid de información de señal
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Columna 1: Intensidad RSSI
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "RSSI",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = "${device.rssi.absoluteValue} dBm",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Columna 2: Distancia estimada
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Distancia",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = "${device.distance.roundToInt()} m",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Columna 3: Categoría
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Categoría",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                val categoryText = when (device.getDistanceCategory()) {
                                    DistanceCategory.CLOSE -> "Cercano"
                                    DistanceCategory.MEDIUM -> "Medio"
                                    DistanceCategory.FAR -> "Lejano"
                                }

                                val categoryColor = when (device.getDistanceCategory()) {
                                    DistanceCategory.CLOSE -> MaterialTheme.colorScheme.primary
                                    DistanceCategory.MEDIUM -> MaterialTheme.colorScheme.tertiary
                                    DistanceCategory.FAR -> MaterialTheme.colorScheme.error
                                }

                                Text(
                                    text = categoryText,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = categoryColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Indicador visual de distancia - CORRECCIÓN AQUÍ
                        val signalIcon = when (device.getDistanceCategory()) {
                            DistanceCategory.CLOSE -> Icons.Default.SignalCellular4Bar
                            DistanceCategory.MEDIUM -> Icons.Default.Bluetooth  // Reemplazado el icono problemático
                            DistanceCategory.FAR -> Icons.Default.SignalCellular0Bar
                        }

                        val signalColor = when (device.getDistanceCategory()) {
                            DistanceCategory.CLOSE -> MaterialTheme.colorScheme.primary
                            DistanceCategory.MEDIUM -> MaterialTheme.colorScheme.tertiary
                            DistanceCategory.FAR -> MaterialTheme.colorScheme.error
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = signalIcon,
                                contentDescription = null,
                                tint = signalColor,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            LinearProgressIndicator(
                                progress = {
                                    when (device.getDistanceCategory()) {
                                        DistanceCategory.CLOSE -> 0.9f
                                        DistanceCategory.MEDIUM -> 0.5f
                                        DistanceCategory.FAR -> 0.2f
                                    }
                                },
                                modifier = Modifier
                                    .height(8.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp)),
                                color = signalColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeCap = StrokeCap.Round
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Información sobre última detección
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = "Última actualización: hace ${(System.currentTimeMillis() - device.lastSeen) / 1000} segundos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Estado de conexión
                    Text(
                        text = "Estado de conexión",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tarjeta de estado de conexión
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = when (connectionState) {
                                ConnectionState.CONNECTED -> MaterialTheme.colorScheme.primaryContainer
                                ConnectionState.CONNECTING, ConnectionState.DISCONNECTING -> MaterialTheme.colorScheme.tertiaryContainer
                                ConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Icono de estado
                            val connectionIcon = when (connectionState) {
                                ConnectionState.CONNECTED -> Icons.Default.BluetoothConnected
                                ConnectionState.CONNECTING -> Icons.Default.BluetoothSearching
                                ConnectionState.DISCONNECTING -> Icons.Default.BluetoothSearching
                                ConnectionState.DISCONNECTED -> Icons.Default.BluetoothDisabled
                            }

                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (connectionState) {
                                            ConnectionState.CONNECTED -> MaterialTheme.colorScheme.primary
                                            ConnectionState.CONNECTING, ConnectionState.DISCONNECTING -> MaterialTheme.colorScheme.tertiary
                                            ConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = connectionIcon,
                                    contentDescription = null,
                                    tint = when (connectionState) {
                                        ConnectionState.CONNECTED -> MaterialTheme.colorScheme.onPrimary
                                        ConnectionState.CONNECTING, ConnectionState.DISCONNECTING -> MaterialTheme.colorScheme.onTertiary
                                        ConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Texto de estado
                            val stateText = when (connectionState) {
                                ConnectionState.CONNECTED -> "Conectado"
                                ConnectionState.CONNECTING -> "Conectando..."
                                ConnectionState.DISCONNECTING -> "Desconectando..."
                                ConnectionState.DISCONNECTED -> "Desconectado"
                            }

                            Text(
                                text = stateText,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (connectionState) {
                                    ConnectionState.CONNECTED -> MaterialTheme.colorScheme.onPrimaryContainer
                                    ConnectionState.CONNECTING, ConnectionState.DISCONNECTING -> MaterialTheme.colorScheme.onTertiaryContainer
                                    ConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Barra de progreso de conexión
                            LinearProgressIndicator(
                                progress = { connectionProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = when (connectionState) {
                                    ConnectionState.CONNECTED -> MaterialTheme.colorScheme.primary
                                    ConnectionState.CONNECTING, ConnectionState.DISCONNECTING -> MaterialTheme.colorScheme.tertiary
                                    ConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                },
                                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Botón de conexión/desconexión
                    AnimatedContent(
                        targetState = connectionState,
                        label = "connectionButton"
                    ) { state ->
                        when (state) {
                            ConnectionState.CONNECTING, ConnectionState.DISCONNECTING -> {
                                // Mostrar spinner durante la conexión/desconexión
                                CircularProgressIndicator(
                                    modifier = Modifier.size(56.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 4.dp
                                )
                            }
                            else -> {
                                Button(
                                    onClick = {
                                        if (isConnected) {
                                            viewModel.disconnectFromDevice()
                                        } else {
                                            viewModel.connectToDevice()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = if (isConnected) {
                                        ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        )
                                    } else {
                                        ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isConnected)
                                                Icons.Default.BluetoothDisabled
                                            else
                                                Icons.Default.BluetoothConnected,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = if (isConnected) "Desconectar" else "Conectar",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Encabezado de la tarjeta
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Contenido personalizado
            content()
        }
    }
}