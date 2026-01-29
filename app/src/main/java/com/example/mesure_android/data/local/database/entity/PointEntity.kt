package com.example.mesure_android.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "measure_points",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId"), Index("previousPointId")]
)
data class PointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val orderIndex: Int,
    // World coordinates
    val worldX: Float,
    val worldY: Float,
    val worldZ: Float,
    // Position relative to origin (QR code)
    val relativeToOriginX: Float,
    val relativeToOriginY: Float,
    val relativeToOriginZ: Float,
    // Chaining to previous point
    val previousPointId: Long? = null,
    val relativeToPreviousX: Float? = null,
    val relativeToPreviousY: Float? = null,
    val relativeToPreviousZ: Float? = null,
    val distanceToPrevious: Float? = null,
    // Label
    val label: String,
    val createdAt: Long = System.currentTimeMillis()
)
