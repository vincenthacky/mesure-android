package com.example.mesure_android.ui.screen.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mesure_android.data.model.QrCodeData
import com.example.mesure_android.data.repository.SiteRepository
import com.example.mesure_android.domain.usecase.ParseQrCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScannerUiState(
    val isScanning: Boolean = false,
    val isProcessing: Boolean = false,
    val scannedQrData: QrCodeData? = null,
    val error: String? = null
)

sealed class ScannerEvent {
    data class NavigateToAr(val siteId: String, val sessionId: Long) : ScannerEvent()
    data class ShowError(val message: String) : ScannerEvent()
}

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val parseQrCodeUseCase: ParseQrCodeUseCase,
    private val siteRepository: SiteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ScannerEvent>()
    val events = _events.asSharedFlow()

    private var lastScannedCode: String? = null

    fun startScanning() {
        _uiState.value = _uiState.value.copy(isScanning = true, error = null)
    }

    fun stopScanning() {
        _uiState.value = _uiState.value.copy(isScanning = false)
    }

    fun onQrCodeScanned(rawJson: String) {
        if (_uiState.value.isProcessing) return
        if (rawJson == lastScannedCode) return

        lastScannedCode = rawJson
        _uiState.value = _uiState.value.copy(isProcessing = true)

        viewModelScope.launch {
            parseQrCodeUseCase(rawJson)
                .onSuccess { qrData ->
                    handleValidQrCode(qrData, rawJson)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        error = "QR code invalide: ${exception.message}"
                    )
                    _events.emit(ScannerEvent.ShowError("QR code invalide"))
                    lastScannedCode = null
                }
        }
    }

    private suspend fun handleValidQrCode(qrData: QrCodeData, rawJson: String) {
        try {
            val site = siteRepository.createOrGetSite(qrData, rawJson)
            val existingSession = siteRepository.getLatestSessionForSite(site.id)

            val session = existingSession ?: siteRepository.createNewSession(site.id)

            _uiState.value = _uiState.value.copy(
                isProcessing = false,
                scannedQrData = qrData
            )

            _events.emit(ScannerEvent.NavigateToAr(site.id, session.id))
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isProcessing = false,
                error = "Erreur: ${e.message}"
            )
            _events.emit(ScannerEvent.ShowError("Erreur lors de la sauvegarde"))
            lastScannedCode = null
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetScanner() {
        lastScannedCode = null
        _uiState.value = ScannerUiState()
    }
}
