package com.cetecom.ibichos.presentation.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cetecom.ibichos.domain.model.CaptureItem
import com.cetecom.ibichos.domain.model.enums.DangerLevel
import com.cetecom.ibichos.presentation.theme.IBichosGreen
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 🎨 PALETA FINAL (verde suave + amarillo)
private val GreenSoft = Color(0xFFE8F5E9)
private val GreenPrimary = Color(0xFF4CAF50)
private val GreenDark = Color(0xFF2E7D32)

private val YellowAccent = Color(0xFFFFC107)
private val YellowSoft = Color(0xFFFFF8E1)

private val BackgroundSoft = Color(0xFFF7FBF8)
private val TextPrimary = Color(0xFF1F2937)
private val TextSecondary = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureDetailScreen(
    capture: CaptureItem,
    currentUserId: String = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "",
    onNavigateBack: () -> Unit,
    onDelete: (String) -> Unit = {},
    onAppeal: (String) -> Unit = {},
    onNavigateToMap: (Double, Double) -> Unit = { _, _ -> }
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val dateStr = remember(capture.capturedAt) {
        if (capture.capturedAt > 0) {
            SimpleDateFormat("dd 'de' MMMM yyyy, HH:mm", Locale("es"))
                .format(Date(capture.capturedAt))
        } else "Fecha desconocida"
    }

    val dangerColor = when (capture.dangerLevel) {
        DangerLevel.HARMLESS -> GreenPrimary
        DangerLevel.CAUTION -> YellowAccent
        DangerLevel.VENOMOUS -> Color(0xFFEF4444)
        DangerLevel.UNKNOWN -> Color(0xFF64748B)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Detalle del Insecto",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .background(GreenSoft, CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, null, tint = GreenDark)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                ),
                actions = {
                    if (currentUserId == capture.userId) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Borrar", tint = Color(0xFFEF4444))
                        }
                    }
                }
            )
        },
        containerColor = BackgroundSoft
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White, GreenSoft)
                    )
                )
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // 📷 Imagen
            val configuration = androidx.compose.ui.platform.LocalConfiguration.current
            val imageHeight = (configuration.screenHeightDp * 0.35).dp

            AsyncImage(
                model = if (capture.imageUrl.startsWith("/")) File(capture.imageUrl) else capture.imageUrl,
                contentDescription = capture.insectName,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )

            // 🐝 Header
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(GreenSoft, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.BugReport,
                            null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            capture.insectName.uppercase(),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary
                        )
                        Text(
                            capture.scientificName,
                            fontStyle = FontStyle.Italic,
                            color = TextSecondary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = dangerColor.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, null, tint = dangerColor, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                capture.dangerLevel.displayName(),
                                color = dangerColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            // 📊 Stats
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .background(YellowSoft, RoundedCornerShape(16.dp))
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    StatBox(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Verified,
                        value = "${(capture.probability * 100).toInt()}%",
                        label = "Confianza IA",
                        color = GreenPrimary
                    )

                    VerticalDivider(
                        modifier = Modifier.height(50.dp),
                        color = GreenPrimary.copy(alpha = 0.2f)
                    )

                    StatBox(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Star,
                        value = "+${capture.xpAwarded} XP",
                        label = "XP",
                        color = YellowAccent
                    )
                }
            }

            InfoCard(Icons.Default.CalendarMonth, "Fecha de captura", dateStr)

            if (capture.latitude != null && capture.longitude != null) {
                InfoCard(
                    icon = Icons.Default.LocationOn,
                    label = "Ubicación GPS",
                    content = "Toca para ver en el mapa",
                    modifier = Modifier.clickable {
                        onNavigateToMap(capture.latitude, capture.longitude)
                    },
                    isLink = true
                )
            }

            if (capture.description.isNotEmpty()) {
                InfoCard(Icons.Default.Eco, "Información Biológica", capture.description)
            }

            if (capture.status == "REJECTED" && currentUserId == capture.userId) {
                Button(
                    onClick = { onAppeal(capture.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC81E1E)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Apelar Decisión", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            
            if (showDeleteDialog) {
                DeleteConfirmDialog(
                    onConfirm = {
                        showDeleteDialog = false
                        onDelete(capture.id)
                    },
                    onDismiss = { showDeleteDialog = false }
                )
            }
        }
    }
}

@Composable
private fun StatBox(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {

        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color)
        }

        Spacer(Modifier.width(8.dp))

        Column {
            Text(value, fontWeight = FontWeight.Bold, color = color)
            Text(label, color = TextSecondary)
        }
    }
}

@Composable
private fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    content: String,
    modifier: Modifier = Modifier,
    isLink: Boolean = false
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(if (isLink) GreenPrimary.copy(alpha = 0.1f) else GreenSoft, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = GreenPrimary)
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(4.dp))
                Text(content, color = if (isLink) GreenPrimary else TextPrimary, fontWeight = if (isLink) FontWeight.Bold else FontWeight.Normal)
            }
            
            if (isLink) {
                Icon(Icons.Default.ChevronRight, null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun DeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Eliminar captura?") },
        text = { Text("Esta acción no se puede deshacer y perderás la XP ganada.") },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))) {
                Text("Eliminar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@androidx.compose.ui.tooling.preview.Preview(name = "Small Phone", widthDp = 320, heightDp = 640, showBackground = true)
@Composable
fun CaptureDetailPreviewSmall() {
    com.cetecom.ibichos.presentation.theme.IBichosTheme {
        CaptureDetailScreen(
            capture = com.cetecom.ibichos.domain.model.CaptureItem(
                id = "1",
                imageUrl = "",
                insectName = "Araña de Rincón",
                scientificName = "Loxosceles laeta",
                category = com.cetecom.ibichos.domain.model.enums.InsectCategory.ARACHNID,
                dangerLevel = com.cetecom.ibichos.domain.model.enums.DangerLevel.VENOMOUS,
                probability = 0.95,
                userId = "user1",
                capturedAt = System.currentTimeMillis(),
                description = "Araña muy venenosa.",
                status = "APPROVED"
            ),
            currentUserId = "user1",
            onNavigateBack = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Medium Phone", widthDp = 411, heightDp = 891, showBackground = true)
@Composable
fun CaptureDetailPreviewMedium() {
    CaptureDetailPreviewSmall()
}

@androidx.compose.ui.tooling.preview.Preview(name = "Large Phone", widthDp = 480, heightDp = 960, showBackground = true)
@Composable
fun CaptureDetailPreviewLarge() {
    CaptureDetailPreviewSmall()
}