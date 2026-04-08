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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateBack: () -> Unit,
    viewModel: MapViewModel = viewModel()
) {
    val context  = LocalContext.current
    val captures by viewModel.captures.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isGlobalMap by viewModel.isGlobalMap.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa de Avistamientos", color = OnDark) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = IBichosGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBackground
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
                        setBuiltInZoomControls(false)
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
                                snippet  = "Precisión: ${(capture.probability * 100).toInt()}% • ${capture.dangerLevel}"
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            mapView.overlays.add(marker)
                        }
                    }

                    // Centrar en la última captura si hay alguna con ubicación
                    val last = captures.lastOrNull { it.latitude != null }
                    if (last?.latitude != null && last.longitude != null) {
                        mapView.controller.animateTo(GeoPoint(last.latitude, last.longitude))
                        mapView.controller.setZoom(15.0)
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
                color = DarkSurface.copy(alpha = 0.95f),
                tonalElevation = 6.dp
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    TextButton(
                        onClick = { viewModel.setGlobalMode(false) },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if (!isGlobalMap) IBichosGreen else androidx.compose.ui.graphics.Color.Transparent,
                            contentColor = if (!isGlobalMap) DarkBackground else androidx.compose.ui.graphics.Color.Gray
                        ),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Text("Mis Insectos")
                    }
                    TextButton(
                        onClick = { viewModel.setGlobalMode(true) },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if (isGlobalMap) IBichosGreen else androidx.compose.ui.graphics.Color.Transparent,
                            contentColor = if (isGlobalMap) DarkBackground else androidx.compose.ui.graphics.Color.Gray
                        ),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Text("Comunidad")
                    }
                }
            }

            // Contador de pines
            if (!isLoading && captures.isNotEmpty()) {
                Surface(
                    modifier      = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    shape         = MaterialTheme.shapes.medium,
                    color         = DarkSurface.copy(alpha = 0.9f),
                    tonalElevation = 4.dp
                ) {
                    Text(
                        text     = "🗺️ ${captures.count { it.latitude != null }} pines en el mapa",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style    = MaterialTheme.typography.labelLarge,
                        color    = OnDark
                    )
                }
            }
        }
    }
}
