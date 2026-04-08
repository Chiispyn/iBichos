package com.cetecom.ibichos.presentation.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cetecom.ibichos.domain.model.CaptureItem
import com.cetecom.ibichos.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureDetailScreen(
    capture: CaptureItem,
    onNavigateBack: () -> Unit
) {
    val dateStr = remember(capture.capturedAt) {
        if (capture.capturedAt > 0) {
            SimpleDateFormat("dd 'de' MMMM yyyy, HH:mm", Locale("es"))
                .format(Date(capture.capturedAt))
        } else "Fecha desconocida"
    }

    val (dangerColor, dangerEmoji) = when (capture.dangerLevel) {
        "Inofensivo", "Mascota Amigable" -> Pair(Color(0xFF4CAF50), "✅")
        "Precaución", "Precaución: Aguijón" -> Pair(IBichosAmber, "⚠️")
        "Venenoso" -> Pair(IBichosOrange, "☠️")
        "Plaga"    -> Pair(IBichosRed, "🚫")
        else       -> Pair(OnDarkSecondary, "❓")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Insecto", color = OnDark) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = IBichosGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Imagen grande ─────────────────────────────────────────────
            AsyncImage(
                model              = if (capture.imageUrl.startsWith("/"))
                    File(capture.imageUrl) else capture.imageUrl,
                contentDescription = capture.insectName,
                modifier           = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentScale       = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Nombre + nivel de peligro
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = capture.insectName,
                            style      = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color      = OnDark
                        )
                        Text(
                            text  = capture.scientificName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnDarkSecondary,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = dangerColor.copy(alpha = 0.2f)
                    ) {
                        Column(
                            modifier            = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(dangerEmoji, style = MaterialTheme.typography.titleLarge)
                            Text(
                                text       = capture.dangerLevel,
                                color      = dangerColor,
                                style      = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                HorizontalDivider(color = DarkOutline)

                // Estadísticas
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(label = "Confianza IA", value = "${(capture.probability * 100).toInt()}%", color = IBichosGreen)
                    StatItem(label = "XP Ganado", value = "+${capture.xpAwarded} XP", color = IBichosTeal)
                }

                // Fecha
                InfoCard(
                    label   = "📅 Fecha de captura",
                    content = dateStr
                )

                // Ubicación
                if (capture.latitude != null && capture.longitude != null) {
                    InfoCard(
                        label   = "📍 Ubicación GPS",
                        content = "Lat: %.4f, Lon: %.4f".format(capture.latitude, capture.longitude)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text       = value,
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color      = color
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelMedium,
            color = OnDarkSecondary
        )
    }
}

@Composable
private fun InfoCard(label: String, content: String) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelLarge,
                color = OnDarkSecondary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = content,
                style = MaterialTheme.typography.bodyMedium,
                color = OnDark
            )
        }
    }
}
