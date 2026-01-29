package com.example.mesure_android.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ArActionButtons(
    onReset: () -> Unit,
    onShowPointsList: () -> Unit,
    onPlacePoint: () -> Unit,
    isReady: Boolean,
    hasPoints: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 40.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Reset calibration button (orange circle)
        FloatingActionButton(
            onClick = onReset,
            containerColor = Color(0xFFFF9800),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reinitialiser"
            )
        }

        // 2. Points list button (blue circle) - only if points exist
        if (hasPoints) {
            Spacer(modifier = Modifier.width(16.dp))
            FloatingActionButton(
                onClick = onShowPointsList,
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FormatListBulleted,
                    contentDescription = "Liste des points"
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 3. Place point button (green capsule with leaf icon + text)
        Button(
            onClick = onPlacePoint,
            enabled = isReady,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50),
                disabledContainerColor = Color.Gray
            ),
            shape = RoundedCornerShape(50),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Placer Point",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}
