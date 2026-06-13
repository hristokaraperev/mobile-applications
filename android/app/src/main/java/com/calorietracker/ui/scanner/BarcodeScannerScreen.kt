package com.calorietracker.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import java.util.concurrent.Executors

/**
 * Barcode scanner screen. Requests the camera permission, shows a CameraX preview with a
 * central scan-area overlay, and feeds frames to ML Kit (EAN-13). A decoded barcode is
 * handed to [BarcodeScannerViewModel], which resolves it to a navigation decision:
 * [onFoodFound] for a known food, or [onUnknownBarcode] to create a custom label.
 */
@Composable
fun BarcodeScannerScreen(
    onFoodFound: (Long) -> Unit,
    onUnknownBarcode: (String) -> Unit,
    viewModel: BarcodeScannerViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        permissionDenied = !granted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(state.navigation) {
        when (val nav = state.navigation) {
            is ScanNavigation.ToFoodDetail -> onFoodFound(nav.foodId)
            is ScanNavigation.ToCustomFood -> onUnknownBarcode(nav.barcode)
            null -> Unit
        }
        if (state.navigation != null) {
            viewModel.onNavigationHandled()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // The preview fills the whole screen, including behind the system bars and
        // the display cutout, so the camera image is full-bleed.
        if (hasPermission) {
            CameraPreview(onBarcode = viewModel::onBarcodeDetected)
        }

        // Overlay controls live inside the safe area so the scan guide, error message,
        // permission rationale, and (future) buttons stay clear of the bars and cutout.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            contentAlignment = Alignment.Center,
        ) {
            when {
                hasPermission -> ScanAreaOverlay()

                else -> CameraPermissionRationale(
                    denied = permissionDenied,
                    onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                )
            }

            state.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(24.dp),
                )
            }
        }
    }
}

/** A square scan-area guide drawn over the preview. */
@Composable
private fun ScanAreaOverlay() {
    Box(
        modifier = Modifier
            .size(240.dp)
            .border(
                width = 3.dp,
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
            )
    )
}

/** Explains why the camera is needed and offers to (re)request the permission. */
@Composable
private fun CameraPermissionRationale(denied: Boolean, onRequest: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = if (denied) {
                "Camera access is needed to scan barcodes. Please allow it to continue."
            } else {
                "Requesting camera access…"
            },
            textAlign = TextAlign.Center,
        )
        if (denied) {
            Button(onClick = onRequest) {
                Text("Grant camera access")
            }
        }
    }
}

/**
 * Renders the CameraX preview and binds an [ImageAnalysis] use case running the ML Kit
 * EAN-13 scanner. Analysis uses keep-only-latest backpressure, so only the most recent
 * frame is processed; decoded values are passed to [onBarcode].
 */
@Composable
private fun CameraPreview(onBarcode: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_EAN_13)
                .build()
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            analysisExecutor.shutdown()
            scanner.close()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val providerFuture = ProcessCameraProvider.getInstance(ctx)
            providerFuture.addListener({
                val provider = providerFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { it.setAnalyzer(analysisExecutor, BarcodeAnalyzer(scanner, onBarcode)) }

                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis,
                )
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
    )
}
