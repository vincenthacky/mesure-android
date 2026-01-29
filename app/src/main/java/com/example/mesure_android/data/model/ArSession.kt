package com.example.mesure_android.data.model

data class ArSession(
    val id: Long = 0,
    val siteId: String,
    val siteName: String,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val origin: Vector3 = Vector3.ZERO,
    val originRotation: Quaternion = Quaternion.IDENTITY,
    val isCalibrated: Boolean = false,
    val points: List<MeasurePoint> = emptyList()
)

data class Quaternion(
    val x: Float,
    val y: Float,
    val z: Float,
    val w: Float
) {
    companion object {
        val IDENTITY = Quaternion(0f, 0f, 0f, 1f)
    }
}
