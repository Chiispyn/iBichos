package com.cetecom.ibichos.presentation.camera

import android.Manifest
import android.app.Activity
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.cetecom.ibichos.presentation.theme.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.io.ByteArrayOutputStream
import android.location.LocationManager
import android.content.Context
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationSettingsRequest


fun isGpsEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

@Composable
fun CameraScreen(viewModel: CameraViewModel = hiltViewModel()) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState        by viewModel.uiState.collectAsStateWithLifecycle()

    // Referencia al use case de CameraX (vive en el Composable, no en el ViewModel)
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var cameraInitError by remember { mutableStateOf<String?>(null) }
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

    val settingResultRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            // El usuario le dio "Aceptar" y el GPS se encendió solo.
            // (El usuario ya puede volver a presionar el botón de foto)
        } else {
            // El usuario le dio "No gracias"
            viewModel.setError("Debes activar el GPS para analizar el insecto.")
        }
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
            if (cameraInitError != null) {
                // ── Error de inicialización de cámara ────────────────────
                Column(
                    modifier            = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("📷", fontSize = 48.sp)
                    Text(
                        text  = cameraInitError ?: "Error al iniciar la cámara",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier  = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else {
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
                                cameraInitError = "No se pudo iniciar la cámara: ${e.message ?: "dispositivo no compatible"}"
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                    }
                }
            )

            // ── OVERLAY DE PREVIEW (FOTO TOMADA) ─────────────────────────
            AnimatedVisibility(
                visible = uiState is CameraUiState.Preview,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (uiState is CameraUiState.Preview) {
                    val previewState = uiState as CameraUiState.Preview
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                        androidx.compose.foundation.Image(
                            bitmap = previewState.bitmap.asImageBitmap(),
                            contentDescription = "Foto capturada",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        
                        // Gradiente para que los botones se vean bien
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                    )
                                )
                        )
                    }
                }
            }
            } // cierre del else (cameraInitError == null)
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
                            is CameraUiState.Preview -> {
                                val state = uiState as CameraUiState.Preview
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "¿Analizar este insecto?",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Asegúrate de que se vea nítido",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(Modifier.height(20.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { viewModel.resetState() },
                                            modifier = Modifier.weight(1f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                        ) {
                                            Text("Repetir", color = MaterialTheme.colorScheme.onSurface)
                                        }
                                        Button(
                                            onClick = { 
                                                viewModel.processCapture(state.bitmap, state.lat, state.lon)
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = IBichosGreen)
                                        ) {
                                            Text("Analizar ✨", color = OnGreen)
                                        }
                                    }
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

                            // 1. CREAMOS LA PETICIÓN DE UBICACIÓN
                            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
                            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
                            val client = LocationServices.getSettingsClient(context)

                            // 2. VERIFICAMOS SI EL GPS ESTÁ ENCENDIDO
                            val task = client.checkLocationSettings(builder.build())

                            task.addOnSuccessListener {
                                // EL GPS ESTÁ ENCENDIDO -> PROCEDEMOS CON LA FOTO Y ANTI-FRAUDE
                                val fusedLocation = LocationServices.getFusedLocationProviderClient(context)
                                val cts = CancellationTokenSource()

                                try {
                                    fusedLocation.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                                        .addOnCompleteListener { locationTask ->
                                            val loc = if (locationTask.isSuccessful) locationTask.result else null

                                            if (loc != null) {
                                                // Lógica Anti-Fraude
                                                if (kotlin.random.Random.nextFloat() < 0.25f) {
                                                    capture.flashMode = androidx.camera.core.ImageCapture.FLASH_MODE_ON
                                                } else {
                                                    capture.flashMode = androidx.camera.core.ImageCapture.FLASH_MODE_AUTO
                                                }

                                                capture.takePicture(
                                                    ContextCompat.getMainExecutor(context),
                                                    object : ImageCapture.OnImageCapturedCallback() {
                                                        override fun onCaptureSuccess(image: ImageProxy) {
                                                            val buffer = image.planes[0].buffer
                                                            val bytes  = ByteArray(buffer.remaining())
                                                            buffer.get(bytes)
                                                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                                            image.close()

                                                            viewModel.setPreview(bitmap, loc.latitude, loc.longitude)
                                                        }
                                                        override fun onError(exc: ImageCaptureException) {
                                                            viewModel.setError("Error del sistema al capturar la imagen.")
                                                        }
                                                    }
                                                )
                                            } else {
                                                viewModel.setError("Calculando ubicación... Presiona el botón de nuevo al aire libre.")
                                            }
                                        }
                                } catch (e: SecurityException) {
                                    viewModel.setError("No tenemos permisos de ubicación.")
                                }
                            }

                            task.addOnFailureListener { exception ->
                                // EL GPS ESTÁ APAGADO -> INTENTAMOS MOSTRAR EL DIÁLOGO MÁGICO
                                if (exception is ResolvableApiException) {
                                    try {
                                        // Esto abre el modal nativo de Android: "¿Activar la ubicación de Google?"
                                        val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                                        settingResultRequest.launch(intentSenderRequest)
                                    } catch (sendEx: Exception) {
                                        viewModel.setError("No se pudo abrir la configuración automática.")
                                    }
                                } else {
                                    viewModel.setError("El GPS está apagado y no se pudo activar automáticamente.")
                                }
                            }
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

