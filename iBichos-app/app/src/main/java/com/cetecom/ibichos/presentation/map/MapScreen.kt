package com.cetecom.ibichos.presentation.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cetecom.ibichos.ui.theme.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import coil.compose.AsyncImage
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import com.cetecom.ibichos.domain.model.CaptureItem
import androidx.compose.material.icons.filled.Close

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (CaptureItem) -> Unit = {},
    initialLat: Double? = null,
    initialLng: Double? = null,
    viewModel: MapViewModel = viewModel()
) {
    var hasCentered by remember { mutableStateOf(false) }
    val context  = LocalContext.current
    val captures by viewModel.captures.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isGlobalMap by viewModel.isGlobalMap.collectAsStateWithLifecycle()
    
    // Estado para la captura seleccionada en el mapa
    var selectedCapture by remember { mutableStateOf<CaptureItem?>(null) }

    // Recarga automática de pines
    LaunchedEffect(Unit) {
        viewModel.loadCaptures()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa de Avistamientos", color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = IBichosGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // ── OSMDroid via AndroidView ──────────────────────────────────
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory  = { ctx ->
                    // OSMDroid requiere configurar el User Agent antes de usar el mapa
                    Configuration.getInstance().userAgentValue = ctx.packageName

                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        setBuiltInZoomControls(true)
                        controller.setZoom(10.0)
                        controller.setCenter(GeoPoint(-33.4489, -70.6693)) // Santiago default
                    }
                },
                update = { mapView ->
                    mapView.overlays.clear()

                    for (capture in captures) {
                        if (capture.latitude != null && capture.longitude != null) {
                            val marker = Marker(mapView).apply {
                                position = GeoPoint(capture.latitude, capture.longitude)
                                title    = capture.insectName
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                
                                // Al tocar el marcador, mostramos nuestra tarjeta personalizada
                                setOnMarkerClickListener { _, _ ->
                                    selectedCapture = capture
                                    mapView.controller.animateTo(GeoPoint(capture.latitude, capture.longitude))
                                    true // Consumimos el click para no mostrar el globo nativo
                                }
                            }
                            mapView.overlays.add(marker)
                        }
                    }

                    // Centrar mapa: Prioridad 1: Coordenadas iniciales, Prioridad 2: Reciente
                    if (!hasCentered && captures.isNotEmpty()) {
                        val targetLat: Double? = initialLat ?: captures.firstOrNull { it.latitude != null }?.latitude
                        val targetLng: Double? = initialLng ?: captures.firstOrNull { it.longitude != null }?.longitude
                        
                        if (targetLat != null && targetLng != null) {
                            mapView.controller.animateTo(GeoPoint(targetLat, targetLng))
                            mapView.controller.setZoom(16.0)
                            hasCentered = true
                        }
                    }

                    mapView.invalidate()
                }
            )

            // Indicador de carga
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color    = IBichosGreen
                )
            }

            // Tabs Switch
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 6.dp
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    TextButton(
                        onClick = { viewModel.setGlobalMode(false) },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if (!isGlobalMap) IBichosGreen else androidx.compose.ui.graphics.Color.Transparent,
                            contentColor = if (!isGlobalMap) MaterialTheme.colorScheme.background else androidx.compose.ui.graphics.Color.Gray
                        ),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Text("Mis Insectos")
                    }
                    TextButton(
                        onClick = { viewModel.setGlobalMode(true) },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if (isGlobalMap) IBichosGreen else androidx.compose.ui.graphics.Color.Transparent,
                            contentColor = if (isGlobalMap) MaterialTheme.colorScheme.background else androidx.compose.ui.graphics.Color.Gray
                        ),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Text("Comunidad")
                    }
                }
            }

            // ── TARJETA DE DETALLE (EL PING CON FOTO) ───────────────────────
            AnimatedVisibility(
                visible = selectedCapture != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .padding(bottom = 60.dp) // Espacio para el contador de pines
            ) {
                selectedCapture?.let { capture ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clickable { onNavigateToDetail(capture) },

                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Miniatura de la foto
                            Card(
                                modifier = Modifier
                                    .size(100.dp)
                                    .aspectRatio(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                AsyncImage(
                                    model = capture.imageUrl,
                                    contentDescription = capture.insectName,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = capture.insectName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    color = IBichosGreen
                                )
                                Text(
                                    text = "Precisión: ${(capture.probability * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = androidx.compose.ui.graphics.Color.Gray
                                )
                                
                                val dangerColor = when (capture.dangerLevel.name) {
                                    "VENOMOUS" -> androidx.compose.ui.graphics.Color(0xFFEF4444)
                                    "CAUTION" -> androidx.compose.ui.graphics.Color(0xFFFFB300)
                                    else -> IBichosGreen
                                }
                                
                                Badge(
                                    containerColor = dangerColor.copy(alpha = 0.1f),
                                    contentColor = dangerColor,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Text(capture.dangerLevel.name, modifier = Modifier.padding(4.dp))
                                }
                            }

                            IconButton(onClick = { selectedCapture = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = androidx.compose.ui.graphics.Color.LightGray)
                            }
                        }
                    }
                }
            }
        }
    }
}

