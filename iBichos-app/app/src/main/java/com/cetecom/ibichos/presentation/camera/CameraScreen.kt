package com.cetecom.ibichos.presentation.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cetecom.ibichos.ui.theme.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.io.ByteArrayOutputStream

@Composable
fun CameraScreen(viewModel: CameraViewModel = viewModel()) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState        by viewModel.uiState.collectAsStateWithLifecycle()

    // Referencia al use case de CameraX (vive en el Composable, no en el ViewModel)
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var hasPermissions by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasPermissions = perms[Manifest.permission.CAMERA] == true &&
                        perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasPermissions) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (hasPermissions) {
            // ── Preview de CameraX via AndroidView ────────────────────────
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory  = { ctx ->
                    PreviewView(ctx).also { previewView ->
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val capture = ImageCapture.Builder().build()
                            imageCapture = capture

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    capture
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                    }
                }
            )
        } else {
            // ── Sin permisos ─────────────────────────────────────────────
            Column(
                modifier            = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("📷", fontSize = 48.sp)
                Text(
                    text  = "Se necesitan permisos de cámara y ubicación",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(Manifest.permission.CAMERA,
                                Manifest.permission.ACCESS_FINE_LOCATION)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IBichosGreen)
                ) {
                    Text("Dar Permiso", color = OnGreen)
                }
            }
        }

        // ── Panel inferior — resultado + botón ────────────────────────────
        Column(
            modifier            = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Resultado de identificación
            AnimatedVisibility(
                visible = uiState !is CameraUiState.Idle,
                enter   = slideInVertically() + fadeIn(),
                exit    = slideOutVertically() + fadeOut()
            ) {
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                ) {
                    Box(
                        modifier            = Modifier.padding(20.dp).fillMaxWidth(),
                        contentAlignment    = Alignment.Center
                    ) {
                        when (val state = uiState) {
                            is CameraUiState.Loading -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier    = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                        color       = IBichosGreen
                                    )
                                    Text(
                                        text  = "Identificando insecto...",
                                        color = MaterialTheme.colorScheme.onBackground,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                            is CameraUiState.Success -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text       = state.message,
                                        color      = IBichosGreen,
                                        fontWeight = FontWeight.Bold,
                                        textAlign  = TextAlign.Center,
                                        style      = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    TextButton(onClick = { viewModel.resetState() }) {
                                        Text("Capturar otro", color = IBichosTeal)
                                    }
                                }
                            }
                            is CameraUiState.Error -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text  = state.message,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center
                                    )
                                    TextButton(onClick = { viewModel.resetState() }) {
                                        Text("Intentar de nuevo", color = IBichosTeal)
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }

            // Botón de captura circular
            if (uiState is CameraUiState.Idle && hasPermissions) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(IBichosGreen),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            val capture = imageCapture ?: return@IconButton
                            
                            // Lógica Anti-Fraude "Arcaica": 25% de probabilidad de forzar flash
                            // Esto genera un reflejo delatador si le están sacando foto a un monitor o libro con brillo.
                            if (kotlin.random.Random.nextFloat() < 0.25f) {
                                capture.flashMode = androidx.camera.core.ImageCapture.FLASH_MODE_ON
                            } else {
                                capture.flashMode = androidx.camera.core.ImageCapture.FLASH_MODE_AUTO
                            }

                            val fusedLocation = LocationServices
                                .getFusedLocationProviderClient(context)

                            capture.takePicture(
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        val buffer = image.planes[0].buffer
                                        val bytes  = ByteArray(buffer.remaining())
                                        buffer.get(bytes)
                                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                        image.close()

                                        // Indicar carga inmediatamente mientras se obtiene el GPS
                                        viewModel.setLoading()

                                         // Intentar obtener ubicación fresca (Priority High Accuracy)
                                         try {
                                             val cts = CancellationTokenSource()
                                             fusedLocation.getCurrentLocation(
                                                 Priority.PRIORITY_HIGH_ACCURACY,
                                                 cts.token
                                             ).addOnCompleteListener { task ->
                                                 val loc = if (task.isSuccessful) task.result else null
                                                 viewModel.processCapture(
                                                     bitmap = bitmap,
                                                     // Fallback: Duoc UC Concepción (si el GPS está apagado o falla)
                                                     lat = loc?.latitude ?: -36.8230,
                                                     lon = loc?.longitude ?: -73.0337,
                                                     context = context
                                                 )
                                             }
                                         } catch (e: SecurityException) {
                                             viewModel.processCapture(bitmap, -36.8230, -73.0337, context)
                                         }
                                    }

                                    override fun onError(exc: ImageCaptureException) {
                                        // El ViewModel no maneja esto porque es lifecycle de CameraX
                                    }
                                }
                            )
                        }
                    ) {
                        Icon(
                            imageVector   = Icons.Default.Camera,
                            contentDescription = "Capturar insecto",
                            tint          = OnGreen,
                            modifier      = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    }
}

