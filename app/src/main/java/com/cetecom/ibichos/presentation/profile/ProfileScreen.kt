package com.cetecom.ibichos.presentation.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.cetecom.ibichos.ui.theme.*

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadAvatar(it) }
    }

    // Mostrar mensajes de éxito/error y luego limpiar
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            kotlinx.coroutines.delay(2500)
            viewModel.clearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Header con gradiente ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.verticalGradient(listOf(IBichosGreenDim, DarkBackground))
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Avatar
                Box(
                    modifier        = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant)
                        .clickable { pickImageLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isUploadingAvatar) {
                        CircularProgressIndicator(color = IBichosGreen, modifier = Modifier.size(40.dp))
                    } else if (!uiState.profile?.avatarUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model              = uiState.profile!!.avatarUrl,
                            contentDescription = "Avatar",
                            modifier           = Modifier.fillMaxSize(),
                            contentScale       = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Avatar por defecto",
                            tint               = OnDarkSecondary,
                            modifier           = Modifier.size(48.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = "Toca para cambiar avatar",
                    color = OnDarkSecondary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator(color = IBichosGreen)
        } else {
            val profile = uiState.profile

            // Nombre
            Text(
                text       = profile?.displayName ?: "Cazador",
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color      = OnDark
            )
            Text(
                text  = profile?.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = OnDarkSecondary
            )

            Spacer(Modifier.height(24.dp))

            // ── Stats ─────────────────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat("⚔️", "${profile?.xp ?: 0}", "XP Total")
                ProfileStat("🏆", profile?.level ?: "Casual", "Nivel")
                ProfileStat("🦟", "${profile?.totalCaptures ?: 0}", "Capturas")
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = DarkOutline, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(Modifier.height(16.dp))

            // ── Nivel / XP Progress ───────────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth()
            ) {
                val xp = profile?.xp ?: 0L
                val nextLevelXp = when {
                    xp < 50   -> 50L
                    xp < 200  -> 200L
                    xp < 500  -> 500L
                    xp < 1000 -> 1000L
                    else      -> 1000L
                }
                val progress = (xp.toFloat() / nextLevelXp).coerceIn(0f, 1f)

                Text(
                    text  = "Progreso de nivel",
                    style = MaterialTheme.typography.labelLarge,
                    color = OnDarkSecondary
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress         = { progress },
                    modifier         = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color            = IBichosGreen,
                    trackColor       = DarkSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = "$xp / $nextLevelXp XP → ${profile?.level ?: "Casual"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnDarkSecondary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(32.dp))

            // Mensajes de estado
            AnimatedVisibility(visible = uiState.successMessage != null) {
                Text(
                    text    = uiState.successMessage ?: "",
                    color   = IBichosGreen,
                    modifier = Modifier.padding(bottom = 8.dp),
                    style   = MaterialTheme.typography.bodyMedium
                )
            }
            AnimatedVisibility(visible = uiState.error != null) {
                Text(
                    text  = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // ── Botón Cerrar Sesión ───────────────────────────────────────
            OutlinedButton(
                onClick  = { viewModel.logout(); onLogout() },
                modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = IBichosRed),
                border   = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp)
            ) {
                Icon(Icons.Default.Logout, null, tint = IBichosRed)
                Spacer(Modifier.width(8.dp))
                Text(
                    text       = "Cerrar Sesión",
                    color      = IBichosRed,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileStat(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 28.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text       = value,
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color      = OnDark
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = OnDarkSecondary
        )
    }
}
