package com.cetecom.ibichos.presentation.ranking

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cetecom.ibichos.presentation.ranking.viewdata.RankingItemViewData
import com.cetecom.ibichos.presentation.theme.IBichosAmber
import com.cetecom.ibichos.presentation.theme.IBichosGreen

private val LightGreen = Color(0xFF5EE6A1)
private val LightGreenDark = Color(0xFF2ED47A)

private val SoftGreen = Color(0xFF6FCF97)
private val SoftGreenLight = Color(0xFFA8E6CF)
private val SoftGreenDark = Color(0xFF27AE60)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(
    viewModel: RankingViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Recarga automática al entrar a la pantalla
    LaunchedEffect(Unit) {
        viewModel.loadRanking()
    }

    RankingContent(
        uiState = uiState,
        onRefresh = { viewModel.loadRanking() },
        onTypeSelected = { viewModel.loadRanking(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingContent(
    uiState: RankingUiState,
    onRefresh: () -> Unit,
    onTypeSelected: (RankingType) -> Unit
) {
    var showInfoDialog by remember { mutableStateOf(false) }

    if (showInfoDialog) {
        RankingInfoDialog(
            onDismiss = { showInfoDialog = false }
        )
    }

    Scaffold(
        topBar = {
            AnimatedRankingTopBar(
                selectedType = uiState.currentType,
                onRulesClick = { showInfoDialog = true },
                onTypeSelected = onTypeSelected
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            LightGreen.copy(alpha = 0.25f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            color = LightGreenDark,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    uiState.error != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = uiState.error!!,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = onRefresh,
                                colors = ButtonDefaults.buttonColors(containerColor = LightGreenDark)
                            ) {
                                Text(
                                    text = "Reintentar",
                                    color = Color.White
                                )
                            }
                        }
                    }

                    uiState.items.isEmpty() -> {
                        Text(
                            text = "Aún no hay exploradores en el ranking.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center
                        )
                    }

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 18.dp,
                                bottom = 100.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(uiState.items) { _, item ->
                                RankingItem(
                                    item = item,
                                    type = uiState.currentType
                                )
                            }
                        }

                        // Tarjeta fija del usuario actual si está en la lista
                        uiState.items.firstOrNull { it.isCurrentUser }?.let { me ->
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp)
                            ) {
                                RankingItem(
                                    item = me,
                                    type = uiState.currentType
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedRankingTopBar(
    selectedType: RankingType,
    onRulesClick: () -> Unit,
    onTypeSelected: (RankingType) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "topbar_animation")

    val trophyScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "trophy_scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.32f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val titleOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "title_offset"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SoftGreen,
                        SoftGreen
                    )
                )
            )
            .padding(top = 10.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.White.copy(alpha = glowAlpha), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = IBichosAmber,
                    modifier = Modifier
                        .size(28.dp)
                        .graphicsLayer {
                            scaleX = trophyScale
                            scaleY = trophyScale
                        }
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        translationY = titleOffset
                    }
            ) {
                Text(
                    text = "Pizarra de Prestigio",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = "Compite con otros exploradores",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            Surface(
                onClick = onRulesClick,
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.18f),
                border = BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = 0.25f)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Reglas",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        RankingTabs(
            selectedType = selectedType,
            onTypeSelected = onTypeSelected
        )
    }
}

@Composable
fun RankingTabs(
    selectedType: RankingType,
    onTypeSelected: (RankingType) -> Unit
) {
    val tabs = listOf(
        RankingType.XP to "Sabios",
        RankingType.UNIQUE to "Especies",
        RankingType.MEDALS to "Medallas"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(Color.White.copy(alpha = 0.16f), RoundedCornerShape(24.dp))
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        tabs.forEach { (type, title) ->
            val selected = selectedType == type

            Surface(
                onClick = { onTypeSelected(type) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                color = if (selected) Color.White else Color.Transparent
            ) {
                Text(
                    text = title,
                    modifier = Modifier.padding(vertical = 11.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = if (selected) LightGreenDark else Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun RankingInfoDialog(
    onDismiss: () -> Unit
) {
    val config = com.cetecom.ibichos.domain.model.enums.GamificationConfig

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(LightGreen.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = LightGreenDark,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Sistema de Prestigio",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📊", fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Ligas Competitivas",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "• Sabios: puntos por descubrir especies.\n• Especies: total de especies distintas.\n• Medallas: insignias desbloqueadas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            lineHeight = 18.sp
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎯", fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Niveles de XP",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        val levels = listOf(
                            "🟢 Casual" to "0",
                            "🔵 Amateur" to "${config.THRESHOLD_AMATEUR}",
                            "🟡 Explorador" to "${config.THRESHOLD_EXPLORER}",
                            "🟠 Entomólogo" to "${config.THRESHOLD_ENTOMOLOGIST}",
                            "🔴 Bug Master" to "${config.THRESHOLD_BUG_MASTER}"
                        )

                        levels.forEach { (name, xp) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.labelMedium
                                )

                                Text(
                                    text = "$xp XP",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = LightGreenDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "¡A Explorar!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingItem(
    item: RankingItemViewData,
    type: RankingType
) {
    // Variable para controlar si el modal está visible o no
    var showDetailsDialog by remember { mutableStateOf(false) }

    val cardColor = if (item.isCurrentUser) Color(0xFFFFF8E1) else Color.White
    val borderColor = if (item.isCurrentUser) LightGreenDark.copy(alpha = 0.65f) else Color.Transparent

    val rank = item.rank

    val rankBg = when (rank) {
        1 -> Color(0xFFFFF3C4)
        2 -> Color(0xFFEDEDED)
        3 -> Color(0xFFFFD8C2)
        else -> Color(0xFFF2F3F4)
    }

    val rankColor = when (rank) {
        1 -> IBichosAmber
        2 -> Color(0xFF777777)
        3 -> Color(0xFFB86B4B)
        else -> Color(0xFF6B6B6B)
    }

    val (icon, label) = when (type) {
        RankingType.XP     -> Icons.Default.Star to "XP Total"
        RankingType.UNIQUE -> Icons.Default.Star to "Bichos"
        RankingType.MEDALS -> Icons.Default.EmojiEvents to "Insignias"
    }

    // Modal de Detalles
    if (showDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp),
            title = null,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F1F1)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!item.avatarUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = item.avatarUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF707070),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (item.isCurrentUser) "Tú (${item.displayName})" else item.displayName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF111111),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = LightGreen.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = item.levelLabel,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightGreenDark
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(icon, null, tint = IBichosAmber, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${item.valueFormatted} $label",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF555555)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDetailsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = LightGreenDark),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Cerrar",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        )
    }

    // Tarjeta del Ranking
    Card(
        // Hacemos que la tarjeta sea clicable para abrir el modal
        onClick = { showDetailsDialog = true },
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (item.isCurrentUser) 1.5.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // Reducimos paddings para aprovechar más el ancho
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                // Contenedor del puesto más pequeño (36.dp en lugar de 46.dp)
                modifier = Modifier
                    .size(36.dp)
                    .background(rankBg, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (rank == 1) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = IBichosAmber,
                        modifier = Modifier.size(20.dp) // Icono de copa más chico
                    )
                } else {
                    Text(
                        text = "$rank",
                        fontSize = 16.sp, // Número del puesto más chico
                        fontWeight = FontWeight.ExtraBold,
                        color = rankColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                // Avatar un poco más pequeño para dar espacio extra
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F1F1)),
                contentAlignment = Alignment.Center
            ) {
                if (!item.avatarUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = item.avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF707070),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            ) {
                Text(
                    text = if (item.isCurrentUser) "Tú" else item.displayName,
                    fontSize = 16.sp, // Nombre un poco más chico
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF111111),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.levelLabel,
                    fontSize = 12.sp, // Etiqueta de nivel más chica
                    fontWeight = FontWeight.Medium,
                    color = LightGreenDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.widthIn(max = 100.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = IBichosAmber,
                        modifier = Modifier.size(16.dp) // Icono de estrella/medalla más chico
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = item.valueFormatted,
                        fontSize = 15.sp, // Puntaje más chico
                        fontWeight = FontWeight.ExtraBold,
                        color = IBichosAmber,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = label,
                    fontSize = 11.sp, // Texto "Bichos" o "XP Total" más chico
                    color = Color(0xFF707070),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
@androidx.compose.ui.tooling.preview.Preview(name = "Small Phone", widthDp = 320, heightDp = 640, showBackground = true)
@Composable
fun RankingPreviewSmall() {
    com.cetecom.ibichos.presentation.theme.IBichosTheme {
        RankingContent(
            uiState = RankingUiState(
                items = listOf(
                    RankingItemViewData(rank = 1, uid = "1", displayName = "Usuario Alfa", avatarUrl = null, levelLabel = "Bug Master", valueFormatted = "1.500 XP", isCurrentUser = false),
                    RankingItemViewData(rank = 2, uid = "2", displayName = "Beta Tester",  avatarUrl = null, levelLabel = "Explorador",  valueFormatted = "1.200 XP", isCurrentUser = true),
                    RankingItemViewData(rank = 3, uid = "3", displayName = "Charlie",       avatarUrl = null, levelLabel = "Casual",      valueFormatted = "900 XP",   isCurrentUser = false)
                ),
                isLoading = false,
                error = null
            ),
            onRefresh = {},
            onTypeSelected = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Medium Phone", widthDp = 411, heightDp = 891, showBackground = true)
@Composable
fun RankingPreviewMedium() {
    RankingPreviewSmall()
}

@androidx.compose.ui.tooling.preview.Preview(name = "Large Phone", widthDp = 480, heightDp = 960, showBackground = true)
@Composable
fun RankingPreviewLarge() {
    RankingPreviewSmall()
}