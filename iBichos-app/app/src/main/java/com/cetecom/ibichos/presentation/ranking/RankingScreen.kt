package com.cetecom.ibichos.presentation.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cetecom.ibichos.domain.model.UserProfile
import com.cetecom.ibichos.ui.theme.IBichosAmber
import com.cetecom.ibichos.ui.theme.IBichosGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(
    viewModel: RankingViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pizarras de Prestigio") },
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

            Spacer(modifier = Modifier.width(16.dp))

            // Info (No Avatar)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isCurrentUser && rank == null) "${user.displayName} (Tú)" else if (isCurrentUser) "Tú" else user.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user.level,
                    style = MaterialTheme.typography.bodySmall,
                    color = IBichosGreen
                )
            }

            // Stat Value (Dynamic)
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (icon, label, value) = when(type) {
                        RankingType.XP -> Triple(Icons.Default.Star, "XP", "${user.xp}")
                        RankingType.UNIQUE -> Triple(Icons.Default.Star, "Especies", "${user.uniqueInsectsCount}")
                        RankingType.MEDALS -> Triple(Icons.Default.EmojiEvents, "Logros", "${user.medals.size}")
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

