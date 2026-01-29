package com.example.mesure_android.ui.screen.ar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mesure_android.ar.ArSessionManager
import com.example.mesure_android.data.local.database.entity.SiteEntity
import com.example.mesure_android.data.model.MeasurePoint
import com.example.mesure_android.data.model.Quaternion
import com.example.mesure_android.data.model.Vector3
import com.example.mesure_android.data.repository.PointRepository
import com.example.mesure_android.data.repository.SiteRepository
import com.example.mesure_android.domain.usecase.CalculateDistanceUseCase
import com.example.mesure_android.domain.usecase.SavePointUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArUiState(
    val site: SiteEntity? = null,
    val sessionId: Long = 0,
    // Calibration
    val isCalibrated: Boolean = false,
    val origin: Vector3 = Vector3.ZERO,
    val isSearchingQRCode: Boolean = true,
    val isReady: Boolean = false,
    // Points
    val points: List<MeasurePoint> = emptyList(),
    val pointCount: Int = 0,
    val lastPointPosition: Vector3? = null,
    val hasExistingData: Boolean = false,
    val existingPointsCount: Int = 0,
    // HUD
    val referenceName: String = "",
    val currentDistance: String = "0.00 m",
    val surfaceCount: Int = 0,
    val statusMessage: String = "Initialisation...",
    val trackingState: String = "Initialisation...",
    val planesDetected: Boolean = false,
    // Toast
    val toastMessage: String? = null,
    // Errors
    val error: String? = null
)

@HiltViewModel
class ArViewModel @Inject constructor(
    private val arSessionManager: ArSessionManager,
    private val siteRepository: SiteRepository,
    private val pointRepository: PointRepository,
    private val savePointUseCase: SavePointUseCase,
    private val calculateDistanceUseCase: CalculateDistanceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArUiState())
    val uiState: StateFlow<ArUiState> = _uiState.asStateFlow()

    private var currentSiteId: String = ""
    private var currentSessionId: Long = 0

    fun initialize(siteId: String, sessionId: Long) {
        currentSiteId = siteId
        currentSessionId = sessionId
        _uiState.value = _uiState.value.copy(sessionId = sessionId)

        viewModelScope.launch {
            // Load site
            val site = siteRepository.getSiteById(siteId)
            _uiState.value = _uiState.value.copy(
                site = site,
                referenceName = site?.name ?: "Site"
            )

            // Load session
            val session = siteRepository.getSessionById(sessionId)
            if (session?.isCalibrated == true) {
                val origin = Vector3(session.originX, session.originY, session.originZ)
                val rotation = Quaternion(session.originQx, session.originQy, session.originQz, session.originQw)
                arSessionManager.setOrigin(origin, rotation)
                _uiState.value = _uiState.value.copy(
                    isCalibrated = true,
                    isSearchingQRCode = false,
                    isReady = true,
                    origin = origin
                )
            }

            // Load existing points
            loadPoints()
        }
    }

    private fun loadPoints() {
        viewModelScope.launch {
            pointRepository.getPointsForSession(currentSessionId).collect { points ->
                val lastPoint = points.lastOrNull()
                _uiState.value = _uiState.value.copy(
                    points = points,
                    pointCount = points.size,
                    lastPointPosition = lastPoint?.worldPosition,
                    hasExistingData = points.isNotEmpty(),
                    existingPointsCount = points.size,
                    referenceName = lastPoint?.label ?: _uiState.value.site?.name ?: "Site"
                )
            }
        }
    }

    fun onPlanesDetected() {
        if (!_uiState.value.planesDetected) {
            _uiState.value = _uiState.value.copy(
                planesDetected = true,
                trackingState = "Plans detectes"
            )
        }
    }

    fun onSurfaceAdded() {
        _uiState.value = _uiState.value.copy(
            surfaceCount = _uiState.value.surfaceCount + 1
        )
    }

    fun updateSurfaceCount(count: Int) {
        if (count != _uiState.value.surfaceCount) {
            _uiState.value = _uiState.value.copy(surfaceCount = count)
        }
    }

    fun updateTrackingState(state: String) {
        _uiState.value = _uiState.value.copy(trackingState = state)
    }

    fun calibrateOrigin(position: Vector3, rotation: Quaternion) {
        viewModelScope.launch {
            arSessionManager.setOrigin(position, rotation)

            siteRepository.calibrateSession(
                sessionId = currentSessionId,
                originX = position.x,
                originY = position.y,
                originZ = position.z,
                qx = rotation.x,
                qy = rotation.y,
                qz = rotation.z,
                qw = rotation.w
            )

            _uiState.value = _uiState.value.copy(
                isCalibrated = true,
                isSearchingQRCode = false,
                isReady = true,
                origin = position,
                statusMessage = "QR Code detecte! Pret a placer des points."
            )
        }
    }

    fun forceCalibration(cameraPosition: Vector3, cameraRotation: Quaternion) {
        calibrateOrigin(cameraPosition, cameraRotation)
    }

    fun resetCalibration() {
        viewModelScope.launch {
            arSessionManager.resetSession()
            _uiState.value = _uiState.value.copy(
                isCalibrated = false,
                isSearchingQRCode = true,
                isReady = false,
                surfaceCount = 0,
                statusMessage = "Recherche du QR Code..."
            )
        }
    }

    fun placePoint(worldPosition: Vector3) {
        if (!_uiState.value.isCalibrated) {
            _uiState.value = _uiState.value.copy(
                error = "Veuillez d'abord calibrer l'origine"
            )
            return
        }

        viewModelScope.launch {
            try {
                val pointCount = _uiState.value.pointCount
                val label = "Arbre ${pointCount + 1}"

                val point = savePointUseCase(
                    sessionId = currentSessionId,
                    worldPosition = worldPosition,
                    origin = _uiState.value.origin,
                    label = label
                )

                _uiState.value = _uiState.value.copy(
                    lastPointPosition = worldPosition,
                    referenceName = label,
                    statusMessage = "$label place et chaine",
                    toastMessage = "$label place et sauvegarde",
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors de la sauvegarde du point"
                )
            }
        }
    }

    fun updateCurrentDistance(cameraPosition: Vector3) {
        val reference = _uiState.value.lastPointPosition ?: if (_uiState.value.isCalibrated) _uiState.value.origin else return
        val distance = calculateDistanceUseCase(reference, cameraPosition)
        val formattedDistance = calculateDistanceUseCase.formatDistance(distance)
        _uiState.value = _uiState.value.copy(currentDistance = formattedDistance)
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun endSession() {
        viewModelScope.launch {
            siteRepository.endSession(currentSessionId)
            arSessionManager.resetSession()
        }
    }

    override fun onCleared() {
        super.onCleared()
        arSessionManager.release()
    }
}
