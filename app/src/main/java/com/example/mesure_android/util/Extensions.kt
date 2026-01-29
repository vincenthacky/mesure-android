package com.example.mesure_android.util

import com.example.mesure_android.data.model.Vector3
import com.google.ar.core.Pose

fun Pose.toVector3(): Vector3 {
    return Vector3(tx(), ty(), tz())
}

fun Vector3.toFloatArray(): FloatArray {
    return floatArrayOf(x, y, z)
}

fun Float.formatAsDistance(): String {
    return when {
        this < 1f -> String.format("%.0f cm", this * 100)
        else -> String.format("%.2f m", this)
    }
}
