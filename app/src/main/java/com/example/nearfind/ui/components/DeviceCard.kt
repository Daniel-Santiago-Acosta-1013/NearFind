package com.example.nearfind.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nearfind.data.model.DistanceCategory
import com.example.nearfind.data.model.NearbyDevice
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceCard(
    device: NearbyDevice,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Determinar el ícono basado en las propiedades del dispositivo
    val deviceIcon = when {
        device.isPaired -> Icons.Default.Check
        device.name.contains("Android", ignoreCase = true) -> Icons.Default.SmartToy
        else -> Icons.Default.Bluetooth
    }

    // Calcular color basado en la categoría de distancia
    val signalColor = when (device.getDistanceCategory()) {
        DistanceCategory.CLOSE -> MaterialTheme.colorScheme.primary
        DistanceCategory.MEDIUM -> MaterialTheme.colorScheme.tertiary
        DistanceCategory.FAR -> MaterialTheme.colorScheme.error
    }

    // Determinar la fuerza de la señal (0.0 a 1.0)
    val signalStrength = when (device.getDistanceCategory()) {
        DistanceCategory.CLOSE -> 0.9f
        DistanceCategory.MEDIUM -> 0.6f
        DistanceCategory.FAR -> 0.3f
    }

    // Animación para el progreso de la señal
    val progressAnimation by animateFloatAsState(
        targetValue = signalStrength,
        label = "signalProgress"
    )

    ElevatedCard(
        onClick = {
            expanded = !expanded
            onClick()
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (device.isPaired)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono del dispositivo
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (device.isPaired)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = deviceIcon,
                    contentDescription = null,
                    tint = if (device.isPaired)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Nombre del dispositivo
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (device.isPaired)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface
                )

                // ID del dispositivo
                Text(
                    text = "ID: ${device.id.take(8)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Usuario (si está disponible)
                if (device.userData != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${if (device.userData.isProfessional) "Pro: " else ""}${device.userData.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (device.userData.isProfessional) FontWeight.Medium else FontWeight.Normal,
                        color = if (device.userData.isProfessional)
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }

                // Indicador de emparejamiento
                if (device.isPaired) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.size(width = 8.dp, height = 8.dp)
                        ) {}

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "Emparejado",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Barra de progreso para la señal
                LinearProgressIndicator(
                    progress = { progressAnimation },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = signalColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Indicador de distancia
            Column(
                horizontalAlignment = Alignment.End
            ) {
                DistanceDisplay(
                    distance = device.distance,
                    category = device.getDistanceCategory()
                )

                // RSSI (fuerza de la señal)
                Text(
                    text = "${device.rssi.absoluteValue} dBm",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Sección expandida con información adicional
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DeviceInfoItem(
                        label = "Última detección",
                        value = "Hace ${(System.currentTimeMillis() - device.lastSeen) / 1000} segundos"
                    )

                    DeviceInfoItem(
                        label = "Estado",
                        value = if (device.isConnected) "Conectado" else "No conectado",
                        valueColor = if (device.isConnected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun DeviceInfoItem(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor
        )
    }
}