package com.example.mesure_android.domain.usecase

import com.example.mesure_android.data.model.Vector3
import javax.inject.Inject
import kotlin.math.sqrt

class CalculateDistanceUseCase @Inject constructor() {
    operator fun invoke(point1: Vector3, point2: Vector3): Float {
        val dx = point2.x - point1.x
        val dy = point2.y - point1.y
        val dz = point2.z - point1.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    fun formatDistance(distanceMeters: Float): String {
        return when {
            distanceMeters < 1f -> String.format("%.0f cm", distanceMeters * 100)
            else -> String.format("%.2f m", distanceMeters)
        }
    }
}
