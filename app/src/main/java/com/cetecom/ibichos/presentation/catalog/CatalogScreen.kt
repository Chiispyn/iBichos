package com.cetecom.ibichos.presentation.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.cetecom.ibichos.domain.model.CaptureItem
import com.cetecom.ibichos.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CatalogScreen(
    onNavigateToMap: () -> Unit,
    onNavigateToDetail: (CaptureItem) -> Unit,
    viewModel: CatalogViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Header ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text       = "Mi Álbum",
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text  = "${uiState.captures.size} insectos capturados",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { viewModel.loadCaptures() }) {
                    Icon(Icons.Default.Refresh, "Recargar", tint = IBichosGreen)
                }
                FilledTonalButton(
                    onClick = onNavigateToMap,
                    colors  = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(Icons.Default.Map, null, tint = IBichosTeal, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Mapa", color = IBichosTeal)
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

        // ── Contenido ─────────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color    = IBichosGreen
                    )
                }
                uiState.captures.isEmpty() -> {
                    Column(
                        modifier            = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🦗", style = MaterialTheme.typography.displayLarge)
                        Text(
                            text      = "Aún no capturaste ningún insecto",
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text      = "¡Usa la cámara para atrapar al primero!",
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            style     = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier        = Modifier.fillMaxSize(),
                        contentPadding  = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.captures, key = { it.id }) { capture ->
                            CaptureCard(
                                capture = capture,
                                onClick = { onNavigateToDetail(capture) }
                            )
                        }
                    }
                }
            }

            // Error overlay
            uiState.error?.let { error ->
                Snackbar(
                    modifier          = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    containerColor    = MaterialTheme.colorScheme.errorContainer,
                    contentColor      = MaterialTheme.colorScheme.onErrorContainer
                ) { Text(error) }
            }
        }
    }
}

@Composable
private fun CaptureCard(capture: CaptureItem, onClick: () -> Unit) {
    val dateStr = remember(capture.capturedAt) {
        if (capture.capturedAt > 0) {
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                .format(Date(capture.capturedAt))
        } else "Fecha desconocida"
    }

    val dangerColor = when (capture.dangerLevel) {
        "Inofensivo", "Mascota Amigable" -> Color(0xFF4CAF50)
        "Precaución", "Precaución: Aguijón" -> IBichosAmber
        "Venenoso", "Plaga" -> IBichosOrange
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.height(100.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen
            AsyncImage(
                model             = if (capture.imageUrl.startsWith("/"))
                    File(capture.imageUrl) else capture.imageUrl,
                contentDescription = capture.insectName,
                modifier          = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)),
                contentScale      = ContentScale.Crop
            )

            // Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text       = capture.insectName,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground,
                    style      = MaterialTheme.typography.titleLarge,
                    maxLines   = 1
                )
                Text(
                    text  = capture.scientificName,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = dangerColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text      = capture.dangerLevel,
                            color     = dangerColor,
                            style     = MaterialTheme.typography.labelSmall,
                            modifier  = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text  = "${(capture.probability * 100).toInt()}% IA",
                        color = IBichosGreen,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text  = dateStr,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

