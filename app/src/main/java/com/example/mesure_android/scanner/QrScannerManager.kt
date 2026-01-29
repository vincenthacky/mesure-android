package com.example.mesure_android.scanner

import android.content.Context
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QrScannerManager @Inject constructor() {

    private var cameraProvider: ProcessCameraProvider? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var barcodeScanner: BarcodeScanner? = null
    private var isScanning = false

    fun startScanning(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onQrCodeDetected: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (isScanning) return
        isScanning = true

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        barcodeScanner = BarcodeScanning.getClient(options)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(
                    cameraProvider!!,
                    lifecycleOwner,
                    previewView,
                    onQrCodeDetected,
                    onError
                )
            } catch (e: Exception) {
                onError(e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCameraUseCases(
        cameraProvider: ProcessCameraProvider,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onQrCodeDetected: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val preview = Preview.Builder()
            .build()
            .also {
                it.surfaceProvider = previewView.surfaceProvider
            }

        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor) { imageProxy ->
                    processImage(imageProxy, onQrCodeDetected)
                }
            }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer
            )
        } catch (e: Exception) {
            onError(e)
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImage(imageProxy: ImageProxy, onQrCodeDetected: (String) -> Unit) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        barcodeScanner?.process(image)
            ?.addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let { rawValue ->
                        onQrCodeDetected(rawValue)
                    }
                }
            }
            ?.addOnCompleteListener {
                imageProxy.close()
            }
    }

    fun stopScanning() {
        isScanning = false
        cameraProvider?.unbindAll()
        barcodeScanner?.close()
        barcodeScanner = null
    }

    fun release() {
        stopScanning()
        cameraExecutor.shutdown()
    }
}
