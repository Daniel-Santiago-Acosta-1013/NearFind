package com.example.nearfind.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nearfind.data.model.DistanceCategory
import kotlin.math.roundToInt

@Composable
fun DistanceDisplay(
    distance: Double,
    category: DistanceCategory
) {
    // Determinar color basado en la categoría
    val color by animateColorAsState(
        targetValue = when (category) {
            DistanceCategory.CLOSE -> MaterialTheme.colorScheme.primary
            DistanceCategory.MEDIUM -> MaterialTheme.colorScheme.tertiary
            DistanceCategory.FAR -> MaterialTheme.colorScheme.error
        },
        animationSpec = tween(300),
        label = "colorAnimation"
    )

    // Determinar ícono basado en la categoría - usando íconos seguros
    val icon: ImageVector = when (category) {
        DistanceCategory.CLOSE -> Icons.Filled.Circle
        DistanceCategory.MEDIUM -> Icons.Filled.Circle
        DistanceCategory.FAR -> Icons.Filled.RadioButtonUnchecked
    }

    // Escala animada para el pulso si está cercano
    val scale by animateFloatAsState(
        targetValue = if (category == DistanceCategory.CLOSE) 1.1f else 1.0f,
        animationSpec = tween(1000),
        label = "scaleAnimation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = color.copy(alpha = 0.15f)
            ),
            modifier = Modifier.scale(scale)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "${distance.roundToInt()} m",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = color
                )
            }
        }

        // Indicador visual de distancia
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(12.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            // Mostrar un "pulso" animado para dispositivos cercanos
            if (category == DistanceCategory.CLOSE) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                )
            }
        }
    }
}