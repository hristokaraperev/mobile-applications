package com.calorietracker.ui.scanner

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage

/**
 * CameraX [ImageAnalysis.Analyzer] that runs ML Kit barcode detection on each frame and
 * reports the first decoded value through [onBarcode]. De-duplication of repeated reads is
 * the consumer's responsibility (see [BarcodeScannerViewModel]); this analyzer simply
 * forwards whatever it decodes and always closes the frame so analysis can continue.
 *
 * @param scanner the configured ML Kit barcode scanner.
 * @param onBarcode invoked with the raw value of the first barcode found in a frame.
 */
class BarcodeAnalyzer(
    private val scanner: BarcodeScanner,
    private val onBarcode: (String) -> Unit,
) : ImageAnalysis.Analyzer {

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()

            return
        }

        val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(input)
            .addOnSuccessListener { barcodes ->
                barcodes.firstNotNullOfOrNull { it.rawValue }?.let(onBarcode)
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}
