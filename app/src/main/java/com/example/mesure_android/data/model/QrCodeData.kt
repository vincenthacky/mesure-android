package com.example.mesure_android.data.model

import com.google.gson.annotations.SerializedName

data class QrCodeData(
    @SerializedName("id")
    val id: String,
    @SerializedName("nom")
    val nom: String,
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lon")
    val lon: Double
)
