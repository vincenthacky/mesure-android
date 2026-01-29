package com.example.mesure_android.domain.usecase

import com.example.mesure_android.data.model.QrCodeData
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import javax.inject.Inject

class ParseQrCodeUseCase @Inject constructor(
    private val gson: Gson
) {
    operator fun invoke(rawJson: String): Result<QrCodeData> {
        return try {
            val qrData = gson.fromJson(rawJson, QrCodeData::class.java)
            if (qrData.id.isBlank() || qrData.nom.isBlank()) {
                Result.failure(IllegalArgumentException("QR code missing required fields"))
            } else {
                Result.success(qrData)
            }
        } catch (e: JsonSyntaxException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
