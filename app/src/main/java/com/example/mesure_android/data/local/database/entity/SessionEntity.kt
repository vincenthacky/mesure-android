package com.example.mesure_android.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = SiteEntity::class,
            parentColumns = ["id"],
            childColumns = ["siteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("siteId")]
)
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val siteId: String,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    // Origin position in world coordinates
    val originX: Float = 0f,
    val originY: Float = 0f,
    val originZ: Float = 0f,
    // Origin rotation as quaternion
    val originQx: Float = 0f,
    val originQy: Float = 0f,
    val originQz: Float = 0f,
    val originQw: Float = 1f,
    val isCalibrated: Boolean = false
)
