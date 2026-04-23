package com.cetecom.ibichos.presentation.ranking

import androidx.compose.foundation.background
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cetecom.ibichos.domain.model.UserProfile
import com.cetecom.ibichos.ui.theme.IBichosAmber
import com.cetecom.ibichos.ui.theme.IBichosGreen
import com.cetecom.ibichos.domain.model.enums.UserLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(
    viewModel: RankingViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showInfoDialog by remember { mutableStateOf(false) }

    // ── Diálogo de ayuda sobre el sistema de gamificación ────────────────────
    if (showInfoDialog) {
        val config = com.cetecom.ibichos.domain.model.enums.GamificationConfig
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(28.dp),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(IBichosGreen.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = IBichosGreen, modifier = Modifier.size(36.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Sistema de Prestigio",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
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
                    // Tarjeta de Reglas
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📊", fontSize = 18.sp)
                                Spacer(Modifier.width(8.dp))
                                Text("Ligas Competitivas", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("• Sabios (XP): Puntos por descubrir especies.\n• Exploradores: Total de especies distintas.\n• Coleccionistas: Medallas desbloqueadas.", 
                                style = MaterialTheme.typography.bodySmall, 
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // Tarjeta de Niveles
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎯", fontSize = 18.sp)
                                Spacer(Modifier.width(8.dp))
                                Text("Niveles de XP", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(name, style = MaterialTheme.typography.labelMedium)
                                    Text("$xp XP", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = IBichosGreen),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("¡A Explorar!", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pizarras de Prestigio") },
                actions = {
                    Surface(
                        onClick = { showInfoDialog = true },
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents, 
                                contentDescription = null,
                                tint = IBichosAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Reglas",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(IBichosGreen.copy(alpha = 0.3f), MaterialTheme.colorScheme.background)
                )
            )
            .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = uiState.currentType.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = IBichosGreen
            ) {
                Tab(
                    selected = uiState.currentType == RankingType.XP,
                    onClick = { viewModel.loadRanking(RankingType.XP) },
                    text = { Text("Sabios (XP)") }
                )
                Tab(
                    selected = uiState.currentType == RankingType.UNIQUE,
                    onClick = { viewModel.loadRanking(RankingType.UNIQUE) },
                    text = { Text("Especies") }
                )
                Tab(
                    selected = uiState.currentType == RankingType.MEDALS,
                    onClick = { viewModel.loadRanking(RankingType.MEDALS) },
                    text = { Text("Medallas") }
                )
            }

            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = IBichosGreen,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (uiState.error != null) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadRanking() }, colors = ButtonDefaults.buttonColors(containerColor = IBichosGreen)) {
                            Text("Reintentar", color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                } else if (uiState.users.isEmpty()) {
                    Text(
                        text = "Aún no hay exploradores en el ranking.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp), // Padding base ajustado por la tarjeta flotante
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
                    
                    // Tarjeta Flotante (Sticky Bottom) para el usuario actual
                    uiState.currentUserProfile?.let { me ->
                        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)) {
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

@Composable
fun RankingItem(rank: Int?, user: UserProfile, type: RankingType, isCurrentUser: Boolean = false) {
    val isTop3 = rank != null && rank <= 3
    val cardColor = when {
        isCurrentUser && !isTop3 -> Color(0xFFE8F5E9) // Verde pastel (Me)
        rank == 1 -> Color(0xFFFFF8E1) // Light Gold
        rank == 2 -> Color(0xFFF5F5F5) // Light Silver
        rank == 3 -> Color(0xFFEFEBE9) // Light Bronze
        else -> MaterialTheme.colorScheme.surface
    }

    val rankIconColor = when (rank) {
        1 -> Color(0xFFFFB300) // Amber 600
        2 -> Color(0xFF9E9E9E) // Grey 500
        3 -> Color(0xFF8D6E63) // Brown 400
        else -> if (isCurrentUser) IBichosGreen else MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isTop3 || isCurrentUser) 6.dp else 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Number/Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color = MaterialTheme.colorScheme.background, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isTop3) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Posición $rank",
                        tint = rankIconColor,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = if (rank != null) "#$rank" else "+50",
                        color = if (isCurrentUser) IBichosAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!user.avatarUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = user.avatarUrl,
                        contentDescription = "Avatar de ${user.displayName}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Sin avatar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isCurrentUser && rank == null) "${user.displayName} (Tú)" else if (isCurrentUser) "Tú" else user.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user.gamification.level.displayName(),
                    style = MaterialTheme.typography.bodySmall,
                    color = IBichosGreen
                )
            }

            // Stat Value (Dynamic)
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (icon, label, value) = when(type) {
                        RankingType.XP     -> Triple(Icons.Default.Star, "XP", "${user.gamification.xp}")
                        RankingType.UNIQUE -> Triple(Icons.Default.Star, "Especies", "${user.gamification.uniqueInsectsCount}")
                        RankingType.MEDALS -> Triple(Icons.Default.EmojiEvents, "Logros", "${user.gamification.medals.size}")
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = IBichosAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium,
                        color = IBichosAmber,
                        fontWeight = FontWeight.Bold
                    )
                }
                val label = when(type) {
                    RankingType.XP -> "XP Total"
                    RankingType.UNIQUE -> "Bichos Diferentes"
                    RankingType.MEDALS -> "Insignias"
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

