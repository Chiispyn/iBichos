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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cetecom.ibichos.domain.model.UserProfile
import com.cetecom.ibichos.ui.theme.IBichosAmber
import com.cetecom.ibichos.ui.theme.IBichosGreen

private val LightGreen = Color(0xFF5EE6A1)
private val LightGreenDark = Color(0xFF2ED47A)

private val SoftGreen = Color(0xFF6FCF97)
private val SoftGreenLight = Color(0xFFA8E6CF)
private val SoftGreenDark = Color(0xFF27AE60)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(
    viewModel: RankingViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showInfoDialog by remember { mutableStateOf(false) }

    // Recarga automática al entrar a la pantalla
    LaunchedEffect(Unit) {
        viewModel.loadRanking()
    }

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
                onTypeSelected = { viewModel.loadRanking(it) }
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
                                onClick = { viewModel.loadRanking() },
                                colors = ButtonDefaults.buttonColors(containerColor = LightGreenDark)
                            ) {
                                Text(
                                    text = "Reintentar",
                                    color = Color.White
                                )
                            }
                        }
                    }

                    uiState.users.isEmpty() -> {
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
                            itemsIndexed(uiState.users) { index, user ->
                                RankingItem(
                                    rank = index + 1,
                                    user = user,
                                    type = uiState.currentType,
                                    isCurrentUser = user.uid == uiState.currentUserId
                                )
                            }
                        }

                        uiState.currentUserProfile?.let { me ->
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp)
                            ) {
                                RankingItem(
                                    rank = uiState.currentUserRank,
                                    user = me,
                                    type = uiState.currentType,
                                    isCurrentUser = true
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

@Composable
fun RankingItem(
    rank: Int?,
    user: UserProfile,
    type: RankingType,
    isCurrentUser: Boolean = false
) {
    val cardColor = if (isCurrentUser) {
        Color(0xFFFFF8E1)
    } else {
        Color.White
    }

    val borderColor = if (isCurrentUser) {
        LightGreenDark.copy(alpha = 0.65f)
    } else {
        Color.Transparent
    }

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

    val (icon, label, value) = when (type) {
        RankingType.XP -> Triple(Icons.Default.Star, "XP Total", "${user.gamification.xp}")
        RankingType.UNIQUE -> Triple(Icons.Default.Star, "Bichos", "${user.gamification.uniqueInsectsCount}")
        RankingType.MEDALS -> Triple(Icons.Default.EmojiEvents, "Insignias", "${user.gamification.medals.size}")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isCurrentUser) 1.5.dp else 0.dp,
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
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(rankBg, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (rank == 1) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = IBichosAmber,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Text(
                        text = "${rank ?: "-"}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = rankColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F1F1)),
                contentAlignment = Alignment.Center
            ) {
                if (!user.avatarUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = user.avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF707070),
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isCurrentUser) "Tú" else user.displayName,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF111111)
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = user.gamification.level.displayName(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = LightGreenDark
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = IBichosAmber,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = value,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = IBichosAmber
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = label,
                    fontSize = 13.sp,
                    color = Color(0xFF707070)
                )
            }
        }
    }
}