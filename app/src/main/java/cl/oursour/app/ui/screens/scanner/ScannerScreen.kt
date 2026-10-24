package cl.oursour.app.ui.screens.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.oursour.app.ui.components.AppScaffold
import cl.oursour.app.ui.theme.Lime
import cl.oursour.app.util.QrAnalyzer
import cl.oursour.app.viewmodel.AppViewModelProvider
import cl.oursour.app.viewmodel.ScanResult
import cl.oursour.app.viewmodel.ScannerViewModel
import java.util.concurrent.Executors
import kotlinx.coroutines.launch

/**
 * Lectura de códigos QR con la cámara (CameraX + ML Kit).
 * Formato: OURSOUR:RECIPE:<id>  u  OURSOUR:PRODUCT:<id>
 */
@Composable
fun ScannerScreen(
    onBack: () -> Unit,
    onOpenRecipe: (Long) -> Unit,
    viewModel: ScannerViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    var busy by remember { mutableStateOf(false) }
    var manualCode by rememberSaveable { mutableStateOf("") }

    // Procesa un código leído (por la cámara o escrito a mano).
    fun handleCode(raw: String) {
        if (busy) return
        busy = true
        viewModel.resolve(raw) { result ->
            when (result) {
                is ScanResult.OpenRecipe -> onOpenRecipe(result.id)
                is ScanResult.Error -> scope.launch {
                    // Mientras se muestra el aviso no se vuelve a escanear (evita repetirlo).
                    snackbar.showSnackbar(result.message)
                    busy = false
                }
            }
        }
    }

    AppScaffold(
        title = "Escanear QR",
        subtitle = "Producto o receta Our Sour",
        onBack = onBack,
        snackbarHostState = snackbar
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (hasPermission) {
                    CameraPreview(
                        onQrDetected = { code -> handleCode(code) },
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        Modifier
                            .size(220.dp)
                            .border(3.dp, Lime, RoundedCornerShape(24.dp))
                    )
                    Text(
                        "Apunta al código QR",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text("📷", style = MaterialTheme.typography.displayMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Necesitamos acceso a la cámara para leer los códigos QR de Our Sour.",
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("Permitir cámara")
                        }
                    }
                }
            }

            // Alternativa para probar sin cámara (útil en el emulador).
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("¿Sin cámara? Simula una lectura", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = manualCode,
                            onValueChange = { manualCode = it },
                            placeholder = { Text("OURSOUR:RECIPE:1") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(onClick = { handleCode(manualCode) }) { Text("Leer") }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("OURSOUR:RECIPE:1", "OURSOUR:RECIPE:13", "OURSOUR:PRODUCT:3").forEach { sample ->
                            AssistChip(onClick = { manualCode = sample }, label = { Text(sample) })
                        }
                    }
                }
            }
        }
    }
}

/** Vista previa de la cámara trasera + análisis de cada cuadro para detectar QR. */
@Composable
private fun CameraPreview(
    onQrDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentCallback by rememberUpdatedState(onQrDetected)
    val previewView = remember { PreviewView(context) }

    DisposableEffect(lifecycleOwner) {
        val executor = Executors.newSingleThreadExecutor()
        val analyzer = QrAnalyzer { code -> currentCallback(code) }
        val providerFuture = ProcessCameraProvider.getInstance(context)

        providerFuture.addListener({
            try {
                val provider = providerFuture.get()
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(previewView.surfaceProvider)

                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(executor, analyzer)

                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            } catch (e: Exception) {
                Log.e("CameraPreview", "No se pudo iniciar la cámara", e)
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            try {
                providerFuture.get().unbindAll()
            } catch (e: Exception) {
                Log.w("CameraPreview", "Error al liberar la cámara", e)
            }
            analyzer.close()
            executor.shutdown()
        }
    }

    AndroidView(factory = { previewView }, modifier = modifier)
}
