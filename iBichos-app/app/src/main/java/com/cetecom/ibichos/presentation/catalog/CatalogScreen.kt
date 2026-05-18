package com.cetecom.ibichos.presentation.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.cetecom.ibichos.domain.model.CaptureItem
import com.cetecom.ibichos.domain.model.enums.DangerLevel
import com.cetecom.ibichos.presentation.theme.IBichosAmber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val LightGreen = Color(0xFFD8F8E9)
private val SoftGreen = Color(0xFFEFFFF6)
private val DarkGreen = Color(0xFF0FA958)
private val TextDark = Color(0xFF092E24)
private val TextGray = Color(0xFF667570)

/* =======================================================
   SCREEN REAL (NO TOCADA)
======================================================= */

@Composable
fun CatalogScreen(
    onNavigateToMap: () -> Unit,
    onNavigateToDetail: (CaptureItem) -> Unit,
    viewModel: CatalogViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Recarga automática al entrar a la pantalla
    LaunchedEffect(Unit) {
        viewModel.loadCaptures()
    }

    CatalogContent(
        captures = uiState.captures,
        isLoading = uiState.isLoading,
        error = uiState.error,
        onRefresh = { viewModel.loadCaptures() },
        onNavigateToMap = onNavigateToMap,
        onNavigateToDetail = onNavigateToDetail
    )
}

/* =======================================================
   CONTENT REUTILIZABLE
======================================================= */

@Composable
private fun CatalogContent(
    captures: List<CaptureItem>,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToDetail: (CaptureItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        LightGreen,
                        SoftGreen,
                        Color.White
                    )
                )
            )
    ) {

        AlbumHeader(
            count = captures.size,
            onRefresh = onRefresh,
            onMapClick = onNavigateToMap
        )

        Box(modifier = Modifier.fillMaxSize()) {

            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = DarkGreen
                    )
                }

                captures.isEmpty() -> {
                    EmptyAlbumState(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 12.dp,
                            end = 12.dp,
                            top = 4.dp,
                            bottom = 100.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(captures, key = { it.id }) { capture ->
                            CaptureCard(
                                capture = capture,
                                onClick = { onNavigateToDetail(capture) }
                            )
                        }
                    }
                }
            }

            error?.let {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(it)
                }
            }
        }
    }
}

/* =======================================================
   HEADER
======================================================= */

@Composable
private fun AlbumHeader(
    count: Int,
    onRefresh: () -> Unit,
    onMapClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(modifier = Modifier.weight(1f)) {

            Text(
                text = "Mi Álbum",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )

            Text(
                text = "$count insectos capturados",
                fontSize = 15.sp,
                color = TextGray
            )
        }

        OutlinedButton(
            onClick = onMapClick,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, DarkGreen)
        ) {
            Icon(Icons.Default.Map, null, tint = DarkGreen)
            Spacer(Modifier.width(6.dp))
            Text("Mapa", color = DarkGreen)
        }
    }
}

/* =======================================================
   CARD
======================================================= */

@Composable
private fun CaptureCard(
    capture: CaptureItem,
    onClick: () -> Unit
) {
    val dateStr = remember(capture.capturedAt) {
        SimpleDateFormat(
            "dd MMM yyyy",
            Locale.getDefault()
        ).format(Date(capture.capturedAt))
    }

    val isRejected = capture.status == "REJECTED"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRejected) Color(0xFFFDE8E8) else Color.White.copy(alpha = 0.96f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isRejected) 2.dp else 8.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxSize().let { if (isRejected) it.alpha(0.6f) else it },
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(82.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(LightGreen),
                contentAlignment = Alignment.Center
            ) {

                if (capture.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = if (capture.imageUrl.startsWith("/"))
                            File(capture.imageUrl)
                        else capture.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("🐞", fontSize = 28.sp)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = capture.insectName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )

                Text(
                    text = capture.scientificName,
                    fontSize = 13.sp,
                    color = TextGray,
                    fontStyle = FontStyle.Italic
                )

                Spacer(Modifier.height(8.dp))

                if (isRejected) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFFBD5D5)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFC81E1E),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = "RECHAZADA",
                                color = Color(0xFFC81E1E),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFFFF3CD)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFFFFA000),
                                modifier = Modifier.size(13.dp)
                            )
    
                            Spacer(Modifier.width(5.dp))
    
                            Text(
                                text = capture.dangerLevel.displayName(),
                                color = Color(0xFFFFA000),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        null,
                        tint = DarkGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = dateStr,
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {

                Text(
                    text = "${(capture.probability * 100).toInt()}%",
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen,
                    fontSize = 16.sp
                )

                Icon(
                    Icons.Default.KeyboardArrowRight,
                    null,
                    tint = DarkGreen
                )
            }
        }
    }
}

/* =======================================================
   EMPTY STATE
======================================================= */

@Composable
private fun EmptyAlbumState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🦗", fontSize = 62.sp)

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Aún no capturaste ningún insecto",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Usa la cámara para atrapar tu primer bicho.",
            textAlign = TextAlign.Center,
            color = TextGray
        )
    }
}

/* =======================================================
   PREVIEWS
======================================================= */

private fun fakeCaptures() = listOf(
    CaptureItem(
        id = "1",
        insectName = "Abeja",
        scientificName = "Apis Mellifera",
        imageUrl = "",
        capturedAt = System.currentTimeMillis(),
        probability = 0.94,
        dangerLevel = DangerLevel.CAUTION
    ),
    CaptureItem(
        id = "2",
        insectName = "Araña",
        scientificName = "Latrodectus",
        imageUrl = "",
        capturedAt = System.currentTimeMillis(),
        probability = 0.88,
        dangerLevel = DangerLevel.CAUTION
    )
)

@Preview(
    name = "Small Phone",
    widthDp = 320,
    heightDp = 640,
    showBackground = true
)
@Composable
fun CatalogPreviewSmall() {
    CatalogContent(
        captures = fakeCaptures(),
        isLoading = false,
        error = null,
        onRefresh = {},
        onNavigateToMap = {},
        onNavigateToDetail = {}
    )
}

@Preview(
    name = "Medium Phone",
    widthDp = 411,
    heightDp = 891,
    showBackground = true
)
@Composable
fun CatalogPreviewMedium() {
    CatalogPreviewSmall()
}

@Preview(
    name = "Large Phone",
    widthDp = 480,
    heightDp = 960,
    showBackground = true
)
@Composable
fun CatalogPreviewLarge() {
    CatalogPreviewSmall()
}