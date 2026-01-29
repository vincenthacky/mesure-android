package com.example.mesure_android.data.model

data class MeasurePoint(
    val id: Long = 0,
    val sessionId: Long,
    val orderIndex: Int,
    val worldPosition: Vector3,
    val relativeToOrigin: Vector3,
    val previousPointId: Long? = null,
    val relativeToPrevious: Vector3? = null,
    val distanceToPrevious: Float? = null,
    val label: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class Vector3(
    val x: Float,
    val y: Float,
    val z: Float
) {
    fun distanceTo(other: Vector3): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
    }

    operator fun minus(other: Vector3): Vector3 {
        return Vector3(x - other.x, y - other.y, z - other.z)
    }

    operator fun plus(other: Vector3): Vector3 {
        return Vector3(x + other.x, y + other.y, z + other.z)
    }

    companion object {
        val ZERO = Vector3(0f, 0f, 0f)
    }
}
