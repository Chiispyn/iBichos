package com.cetecom.ibichos.presentation.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.cetecom.ibichos.domain.model.enums.MedalInfo
import com.cetecom.ibichos.ui.theme.*

private val LightGreen = Color(0xFF5EE6A1)
private val DarkGreen = Color(0xFF0FA958)
private val SoftGreen = Color(0xFFEFFFF6)

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedMedal by remember { mutableStateOf<MedalInfo?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadAvatar(it, context) }
    }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            kotlinx.coroutines.delay(2500)
            viewModel.clearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        LightGreen.copy(alpha = 0.75f),
                        SoftGreen,
                        Color.White
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileHeader(
            avatarUrl = uiState.profile?.avatarUrl,
            isUploading = uiState.isUploadingAvatar,
            name = uiState.profile?.displayName ?: "Cazador",
            email = uiState.profile?.email ?: "",
            onAvatarClick = { pickImageLauncher.launch("image/*") }
        )

        if (uiState.isLoading) {
            Spacer(Modifier.height(40.dp))
            CircularProgressIndicator(color = DarkGreen)
        } else {
            val profile = uiState.profile
            val xp = profile?.gamification?.xp ?: 0L
            val nextLevelXp =
                com.cetecom.ibichos.domain.model.enums.GamificationConfig.getNextLevelXp(xp)
            val progress = (xp.toFloat() / nextLevelXp).coerceIn(0f, 1f)
            val level = profile?.gamification?.level?.displayName() ?: "Casual"

            ProfileStatsCard(
                xp = xp,
                level = level,
                captures = profile?.totalCaptures ?: 0
            )

            ProfileProgressCard(
                xp = xp,
                nextLevelXp = nextLevelXp,
                progress = progress,
                level = level
            )

            if (profile?.gamification?.medals?.isNotEmpty() == true) {
                AchievementsCard(
                    medals = profile.gamification.medals,
                    medalsEarnedAt = profile.gamification.medalsEarnedAt,
                    onMedalClick = { selectedMedal = it }
                )
            }

            Spacer(Modifier.height(24.dp))

            AnimatedVisibility(visible = uiState.successMessage != null) {
                Text(
                    text = uiState.successMessage ?: "",
                    color = DarkGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }

            AnimatedVisibility(visible = uiState.error != null) {
                Text(
                    text = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(24.dp))

            LogoutButton {
                viewModel.logout()
                onLogout()
            }
        }
    }

    selectedMedal?.let { medal ->
        AlertDialog(
            onDismissRequest = { selectedMedal = null },
            icon = { Text(medal.icon, fontSize = 48.sp) },
            title = {
                Text(
                    text = medal.title,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = medal.description,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(onClick = { selectedMedal = null }) {
                    Text(
                        text = "Genial",
                        color = DarkGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun ProfileHeader(
    avatarUrl: String?,
    isUploading: Boolean,
    name: String,
    email: String,
    onAvatarClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "profile_header_animation")

    val leafOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leaf_offset_1"
    )

    val leafRotation1 by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leaf_rotation_1"
    )

    val leafOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -18f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leaf_offset_2"
    )

    val leafRotation2 by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leaf_rotation_2"
    )

    val particleOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -18f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle_offset_1"
    )

    val particleOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -25f,
        animationSpec = infiniteRepeatable(
            animation = tween(3400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle_offset_2"
    )

    val particleOffset3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -14f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle_offset_3"
    )

    val particleOffset4 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle_offset_4"
    )

    val particleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(355.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        FloatingParticle(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 78.dp, top = 110.dp)
                .graphicsLayer {
                    translationY = particleOffset1
                    alpha = particleAlpha
                }
        )

        FloatingParticle(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 82.dp, top = 135.dp)
                .graphicsLayer {
                    translationY = particleOffset2
                    alpha = particleAlpha
                }
        )

        FloatingParticle(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = 170.dp, bottom = 80.dp)
                .graphicsLayer {
                    translationY = particleOffset3
                    alpha = particleAlpha
                }
        )

        FloatingParticle(
            size = 6,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 95.dp, end = 160.dp)
                .graphicsLayer {
                    translationY = particleOffset4
                    alpha = particleAlpha
                }
        )

        Text(
            text = "🌿",
            fontSize = 58.sp,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp, top = 80.dp)
                .graphicsLayer {
                    translationY = leafOffset1
                    rotationZ = leafRotation1
                }
        )

        Text(
            text = "🌿",
            fontSize = 68.sp,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 6.dp, top = 120.dp)
                .graphicsLayer {
                    translationY = leafOffset2
                    rotationZ = leafRotation2
                }
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 48.dp)
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(118.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF5722))
                        .clickable { onAvatarClick() },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isUploading -> CircularProgressIndicator(color = Color.White)

                        !avatarUrl.isNullOrEmpty() -> {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        else -> {
                            Text(
                                text = name.firstOrNull()?.uppercase() ?: "C",
                                fontSize = 62.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = DarkGreen,
                    border = BorderStroke(4.dp, Color.White),
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(9.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Toca para cambiar avatar",
                fontSize = 15.sp,
                color = Color(0xFF5C6770)
            )

            Spacer(Modifier.height(22.dp))

            Text(
                text = name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF162326),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Text(
                text = email,
                fontSize = 16.sp,
                color = Color(0xFF5C6770),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
private fun FloatingParticle(
    modifier: Modifier = Modifier,
    size: Int = 9
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .background(
                color = Color(0xFFFFF6A5).copy(alpha = 0.85f),
                shape = CircleShape
            )
    )
}

@Composable
private fun ProfileStatsCard(
    xp: Long,
    level: String,
    captures: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileStat("⚔️", "$xp", "XP Total")
            VerticalDivider()
            ProfileStat("🏆", level, "Nivel")
            VerticalDivider()
            ProfileStat("🦟", "$captures", "Capturas")
        }
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .height(75.dp)
            .width(1.dp)
            .background(Color(0xFFE3E8E6))
    )
}

@Composable
private fun ProfileProgressCard(
    xp: Long,
    nextLevelXp: Long,
    progress: Float,
    level: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 18.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(54.dp),
                    shape = CircleShape,
                    color = Color(0xFFE6FFF1),
                    border = BorderStroke(1.dp, LightGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = DarkGreen,
                        modifier = Modifier.padding(13.dp)
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Progreso de nivel",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF162326)
                    )

                    Text(
                        text = "Sigue explorando para subir de nivel",
                        fontSize = 14.sp,
                        color = Color(0xFF5C6770)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFE8F8EF)
                ) {
                    Text(
                        text = "$xp / $nextLevelXp XP",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        color = DarkGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(50)),
                color = DarkGreen,
                trackColor = Color(0xFFE2ECE7)
            )

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$xp / $nextLevelXp XP",
                    color = Color(0xFF0B5D35),
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = level,
                    color = DarkGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AchievementsCard(
    medals: List<String>,
    medalsEarnedAt: Map<String, Long>,
    onMedalClick: (MedalInfo) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = Color(0xFF2ECC71)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(13.dp)
                    )
                }

                Spacer(Modifier.width(14.dp))

                Text(
                    text = "Logros Desbloqueados",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF162326),
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = DarkGreen
                )
            }

            Spacer(Modifier.height(18.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                medals.forEach { medalId ->
                    val medalInfo = MedalInfo.fromId(medalId)
                    val title = medalInfo?.title ?: medalId
                    val icon = medalInfo?.icon ?: "🏅"

                    val timestamp = medalsEarnedAt[medalId]
                    val dateStr = if (timestamp != null) {
                        val sdf = java.text.SimpleDateFormat(
                            "dd/MM/yyyy",
                            java.util.Locale.getDefault()
                        )
                        sdf.format(java.util.Date(timestamp))
                    } else {
                        "Fecha desconocida"
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (medalInfo != null) onMedalClick(medalInfo)
                            },
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFF1FBF6)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = icon, fontSize = 34.sp)

                            Spacer(Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF162326)
                                )

                                Text(
                                    text = dateStr,
                                    fontSize = 14.sp,
                                    color = Color(0xFF5C6770)
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color(0xFF5C6770)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogoutButton(
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 22.dp)
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = IBichosRed,
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, Color(0xFFE1E5E3))
    ) {
        Icon(
            imageVector = Icons.Default.Logout,
            contentDescription = null,
            tint = IBichosRed,
            modifier = Modifier.size(28.dp)
        )

        Spacer(Modifier.width(10.dp))

        Text(
            text = "Cerrar Sesión",
            fontSize = 17.sp,
            color = IBichosRed,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProfileStat(
    emoji: String,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(95.dp)
    ) {
        Text(emoji, fontSize = 32.sp)

        Spacer(Modifier.height(6.dp))

        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = DarkGreen,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF5C6770),
            textAlign = TextAlign.Center
        )
    }
}
