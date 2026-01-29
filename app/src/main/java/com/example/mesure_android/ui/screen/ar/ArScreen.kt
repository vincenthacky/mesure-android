package com.example.mesure_android.ui.screen.ar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mesure_android.data.model.Quaternion
import com.example.mesure_android.data.model.Vector3
import com.example.mesure_android.ui.component.ArActionButtons
import com.example.mesure_android.ui.component.HudOverlay
import com.example.mesure_android.ui.component.PointsListSheet
import com.example.mesure_android.ui.component.SearchingOverlay
import com.google.ar.core.Config
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import dev.romainguy.kotlin.math.Float4
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.node.SphereNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberNodes
import kotlinx.coroutines.delay

/**
 * Shared mutable action queue readable from onSessionUpdated callback.
 * Compose state captured in ARScene lambda can become stale,
 * so we use a plain object reference instead.
 */
private class PendingActions {
    var placePoint = false
    var forceCalibration = false
}

@Composable
fun ArScreen(
    siteId: String,
    sessionId: Long,
    onBack: () -> Unit,
    viewModel: ArViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(siteId, sessionId) {
        viewModel.initialize(siteId, sessionId)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.endSession()
        }
    }

    val engine = rememberEngine()
    val materialLoader = rememberMaterialLoader(engine)
    val childNodes = rememberNodes()

    // Mutable action queue - survives recomposition AND is visible from ARScene callback
    val pendingActions = remember { PendingActions() }

    var showPointsList by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // AR Scene
        ARScene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            materialLoader = materialLoader,
            childNodes = childNodes,
            planeRenderer = true,
            sessionConfiguration = { session, config ->
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                config.focusMode = Config.FocusMode.AUTO
            },
            onSessionUpdated = { session, frame ->
                // Check for planes and count surfaces
                val planes = session.getAllTrackables(Plane::class.java)
                val trackingPlanes = planes.filter { it.trackingState == TrackingState.TRACKING }
                if (trackingPlanes.isNotEmpty()) {
                    viewModel.onPlanesDetected()
                }
                // Update surface count directly
                val surfaceCount = trackingPlanes.size
                viewModel.updateSurfaceCount(surfaceCount)

                // Update tracking state
                when (frame.camera.trackingState) {
                    TrackingState.TRACKING -> {
                        if (trackingPlanes.isNotEmpty()) {
                            viewModel.updateTrackingState("Suivi actif")
                        } else {
                            viewModel.updateTrackingState("Recherche de plans...")
                        }

                        val cameraPose = frame.camera.pose
                        val cameraPosition = Vector3(
                            cameraPose.tx(),
                            cameraPose.ty(),
                            cameraPose.tz()
                        )

                        // Update distance in real-time
                        viewModel.updateCurrentDistance(cameraPosition)

                        // Handle force calibration (manual calibration button)
                        if (pendingActions.forceCalibration) {
                            pendingActions.forceCalibration = false

                            val pos = Vector3(cameraPose.tx(), cameraPose.ty(), cameraPose.tz())
                            val rot = Quaternion(
                                cameraPose.qx(), cameraPose.qy(),
                                cameraPose.qz(), cameraPose.qw()
                            )
                            viewModel.forceCalibration(pos, rot)

                            // Add origin marker (blue sphere)
                            try {
                                val anchor = session.createAnchor(cameraPose)
                                val originMaterial = materialLoader.createColorInstance(
                                    color = Float4(0f, 0.4f, 1f, 1f)
                                )
                                val originNode = SphereNode(
                                    engine = engine,
                                    radius = 0.05f,
                                    materialInstance = originMaterial
                                )
                                val anchorNode = AnchorNode(
                                    engine = engine,
                                    anchor = anchor
                                ).apply {
                                    addChildNode(originNode)
                                }
                                childNodes.add(anchorNode)
                            } catch (e: Exception) {
                                // Anchor creation can fail, calibration still works
                            }
                        }

                        // Handle point placement using CAMERA POSITION (like iOS)
                        if (pendingActions.placePoint) {
                            pendingActions.placePoint = false

                            val pos = Vector3(
                                cameraPose.tx(),
                                cameraPose.ty(),
                                cameraPose.tz()
                            )
                            viewModel.placePoint(pos)

                            // Add green sphere at camera position
                            try {
                                val anchor = session.createAnchor(cameraPose)
                                val pointMaterial = materialLoader.createColorInstance(
                                    color = Float4(0.2f, 0.8f, 0.2f, 1f)
                                )
                                val pointNode = SphereNode(
                                    engine = engine,
                                    radius = 0.04f,
                                    materialInstance = pointMaterial
                                )
                                val anchorNode = AnchorNode(
                                    engine = engine,
                                    anchor = anchor
                                ).apply {
                                    addChildNode(pointNode)
                                }
                                childNodes.add(anchorNode)
                            } catch (e: Exception) {
                                // Anchor creation can fail
                            }
                        }
                    }
                    TrackingState.PAUSED -> viewModel.updateTrackingState("Suivi en pause")
                    TrackingState.STOPPED -> viewModel.updateTrackingState("Suivi arrete")
                }
            },
            onSessionCreated = { session ->
                // Session created
            }
        )

        // Searching overlay (shown while not calibrated, like iOS)
        if (uiState.isSearchingQRCode && !uiState.isCalibrated) {
            SearchingOverlay(
                qrCodeName = uiState.site?.name ?: "Site",
                hasExistingData = uiState.hasExistingData,
                existingPointsCount = uiState.existingPointsCount,
                onForceCalibration = {
                    pendingActions.forceCalibration = true
                }
            )
        }

        // HUD Overlay (only shown after calibration, like iOS)
        if (uiState.isCalibrated) {
            HudOverlay(
                referenceName = uiState.referenceName,
                distance = uiState.currentDistance,
                surfaceCount = uiState.surfaceCount,
                pointCount = uiState.pointCount,
                statusMessage = uiState.statusMessage,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        // Bottom action buttons (only shown after calibration, like iOS)
        if (uiState.isCalibrated) {
            ArActionButtons(
                onReset = {
                    viewModel.resetCalibration()
                    childNodes.clear()
                },
                onShowPointsList = { showPointsList = true },
                onPlacePoint = {
                    pendingActions.placePoint = true
                },
                isReady = uiState.isReady,
                hasPoints = uiState.pointCount > 0,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // Status message (shown below HUD when calibrated)
        if (uiState.isCalibrated && uiState.statusMessage.isNotEmpty()) {
            Text(
                text = uiState.statusMessage,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 180.dp)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(50)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Toast confirmation (like iOS)
        uiState.toastMessage?.let { message ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
            ) {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                Color(0xFF4CAF50),
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = message,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            LaunchedEffect(message) {
                delay(2000)
                viewModel.clearToast()
            }
        }
    }

    // Points list bottom sheet (like iOS sheet)
    if (showPointsList) {
        PointsListSheet(
            points = uiState.points,
            siteName = uiState.site?.name ?: "Site",
            siteId = uiState.site?.id ?: "",
            onDismiss = { showPointsList = false }
        )
    }
}
