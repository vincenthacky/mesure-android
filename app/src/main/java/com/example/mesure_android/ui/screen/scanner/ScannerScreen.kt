package com.example.mesure_android.ui.screen.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mesure_android.scanner.QrScannerManager

@Composable
fun ScannerScreen(
    onNavigateToAr: (siteId: String, sessionId: Long) -> Unit,
    onNavigateToSessions: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    val qrScannerManager = remember { QrScannerManager() }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ScannerEvent.NavigateToAr -> {
                    qrScannerManager.stopScanning()
                    onNavigateToAr(event.siteId, event.sessionId)
                }
                is ScannerEvent.ShowError -> {
                    // Error is shown in UI
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            qrScannerManager.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            CameraPreview(
                qrScannerManager = qrScannerManager,
                lifecycleOwner = lifecycleOwner,
                onQrCodeDetected = { viewModel.onQrCodeScanned(it) },
                modifier = Modifier.fillMaxSize()
            )

            // Scanning frame overlay
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .border(
                            width = 3.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp)
                        )
                )
            }

            // Top overlay with instructions
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = "Scanner le QR Code",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Positionnez le QR code dans le cadre",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // Bottom overlay with sessions button
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextButton(
                    onClick = onNavigateToSessions,
                    modifier = Modifier
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Sessions sauvegardees",
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Processing overlay
            if (uiState.isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Chargement...",
                            color = Color.White
                        )
                    }
                }
            }

            // Error display
            uiState.error?.let { error ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = error,
                        color = Color.Red,
                        modifier = Modifier
                            .background(
                                Color.Black.copy(alpha = 0.7f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(16.dp)
                    )
                }
            }
        } else {
            // No camera permission
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Permission camera requise",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "L'application necessite l'acces a la camera pour scanner les QR codes",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                ) {
                    Text("Autoriser la camera")
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(
    qrScannerManager: QrScannerManager,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    onQrCodeDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).also { previewView ->
                qrScannerManager.startScanning(
                    context = ctx,
                    lifecycleOwner = lifecycleOwner,
                    previewView = previewView,
                    onQrCodeDetected = onQrCodeDetected,
                    onError = { /* Handle error */ }
                )
            }
        },
        modifier = modifier
    )
}
