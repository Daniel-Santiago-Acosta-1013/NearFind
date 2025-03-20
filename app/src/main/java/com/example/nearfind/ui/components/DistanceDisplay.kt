package com.example.nearfind.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.nearfind.data.model.DistanceCategory
import kotlin.math.roundToInt

@Composable
fun DistanceDisplay(
    distance: Double,
    category: DistanceCategory
) {
    val backgroundColor = when (category) {
        DistanceCategory.CLOSE -> Color.Green
        DistanceCategory.MEDIUM -> Color.Yellow
        DistanceCategory.FAR -> Color.Red
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(backgroundColor)
        )

        Text(
            text = "${distance.roundToInt()} m",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}