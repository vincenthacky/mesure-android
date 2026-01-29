package com.example.mesure_android.ui.component

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HudOverlay(
    referenceName: String,
    distance: String,
    surfaceCount: Int,
    pointCount: Int,
    statusMessage: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // HUD info box (matches iOS .ultraThinMaterial rounded rectangle)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.Black.copy(alpha = 0.5f),
                    RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HudRow(
                icon = Icons.Default.LocationOn,
                label = "Ref",
                value = referenceName
            )
            HudRow(
                icon = Icons.Default.Straighten,
                label = "Distance",
                value = distance.ifEmpty { "0.00 m" }
            )
            HudRow(
                icon = Icons.Default.Layers,
                label = "Surfaces",
                value = "$surfaceCount"
            )
            HudRow(
                icon = Icons.Default.Eco,
                label = "Points places",
                value = "$pointCount"
            )
        }
    }
}

@Composable
private fun HudRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "$label:",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
