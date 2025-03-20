package com.example.nearfind.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nearfind.data.model.DistanceCategory
import com.example.nearfind.data.model.NearbyDevice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceCard(
    device: NearbyDevice,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
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
                    text = device.id,
                    style = MaterialTheme.typography.bodySmall
                )

                if (device.userData != null) {
                    Text(
                        text = "Usuario: ${device.userData.name}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (device.isPaired) {
                    Text(
                        text = "Emparejado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            DistanceDisplay(
                distance = device.distance,
                category = device.getDistanceCategory()
            )
        }
    }
}