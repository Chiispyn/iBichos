
package com.cetecom.ibichos.presentation.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.cetecom.ibichos.domain.model.CaptureItem
import com.cetecom.ibichos.ui.theme.IBichosAmber
import com.cetecom.ibichos.ui.theme.IBichosOrange
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val LightGreen = Color(0xFFD8F8E9)
private val SoftGreen = Color(0xFFEFFFF6)
private val DarkGreen = Color(0xFF0FA958)
private val TextDark = Color(0xFF092E24)
private val TextGray = Color(0xFF667570)

@Composable
fun CatalogScreen(
    onNavigateToMap: () -> Unit,
    onNavigateToDetail: (CaptureItem) -> Unit,
    viewModel: CatalogViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Recarga automática al entrar a la pantalla
    LaunchedEffect(Unit) {
        viewModel.loadCaptures()
    }

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
            count = uiState.captures.size,
            onRefresh = { viewModel.loadCaptures() },
            onMapClick = onNavigateToMap
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = DarkGreen
                    )
                }

                uiState.captures.isEmpty() -> {
                    EmptyAlbumState(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 18.dp,
                            end = 18.dp,
                            top = 4.dp,
                            bottom = 110.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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

            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Text(error)
                }
            }
        }
    }
}

@Composable
private fun AlbumHeader(
    count: Int,
    onRefresh: () -> Unit,
    onMapClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp, bottom = 22.dp)
    ) {
        Text(
            text = "🍃",
            fontSize = 42.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(start = 62.dp, top = 4.dp)
        )

        Text(
            text = "🌿",
            fontSize = 46.sp,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 0.dp, top = 20.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Mi Álbum",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )

                Text(
                    text = "$count insectos capturados",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextGray
                )
            }

            Surface(
                onClick = onRefresh,
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 6.dp
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Recargar",
                    tint = DarkGreen,
                    modifier = Modifier.padding(15.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Surface(
                onClick = onMapClick,
                shape = RoundedCornerShape(50),
                color = Color.White,
                border = BorderStroke(1.5.dp, DarkGreen),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = DarkGreen,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "Mapa",
                        color = DarkGreen,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CaptureCard(
    capture: CaptureItem,
    onClick: () -> Unit
) {
    val dateStr = remember(capture.capturedAt) {
        if (capture.capturedAt > 0) {
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                .format(Date(capture.capturedAt))
        } else {
            "Fecha desconocida"
        }
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
                    .width(126.dp)
                    .fillMaxHeight()
                    .padding(8.dp)
            ) {
                AsyncImage(
                    model = if (capture.imageUrl.startsWith("/")) {
                        File(capture.imageUrl)
                    } else {
                        capture.imageUrl
                    },
                    contentDescription = capture.insectName,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(alpha = 0.58f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )

                        Spacer(Modifier.width(5.dp))

                        Text(
                            text = "1",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, end = 8.dp, top = 18.dp, bottom = 14.dp)
            ) {
                Text(
                    text = capture.insectName.uppercase(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark,
                    maxLines = 1
                )

                Text(
                    text = capture.scientificName,
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Italic,
                    color = TextGray,
                    maxLines = 1
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

                Spacer(Modifier.weight(1f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = DarkGreen,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(Modifier.width(7.dp))

                    Text(
                        text = dateStr,
                        fontSize = 13.sp,
                        color = TextGray,
                        maxLines = 1
                    )
                }
            }

            Row(
                modifier = Modifier.padding(end = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFF0FAEC)
                ) {
                    Text(
                        text = "${(capture.probability * 100).toInt()}% IA",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                        color = DarkGreen,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(Modifier.width(6.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFF0FAEC)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = DarkGreen,
                        modifier = Modifier
                            .size(42.dp)
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyAlbumState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🦗",
            fontSize = 66.sp
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Aún no capturaste ningún insecto",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Usa la cámara para atrapar tu primer bicho.",
            fontSize = 15.sp,
            color = TextGray,
            textAlign = TextAlign.Center
        )
    }
}