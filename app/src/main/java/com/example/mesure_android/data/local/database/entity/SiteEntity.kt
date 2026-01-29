package com.example.mesure_android.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sites")
data class SiteEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: Long = System.currentTimeMillis(),
    val rawQrData: String
)
