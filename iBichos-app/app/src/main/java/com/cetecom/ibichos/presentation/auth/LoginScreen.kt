package com.cetecom.ibichos.presentation.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cetecom.ibichos.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }
    var showRecoverDialog by remember { mutableStateOf(false) }
    var recoverEmail by remember { mutableStateOf("") }

    val webClientId =
        "996905357020-0vekhfrin7f4k8mm19euss39crisducm.apps.googleusercontent.com"

    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        GoogleSignIn.getClient(context, gso)
    }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.handleGoogleResult(it)
    }

    LaunchedEffect(uiState.user) {
        if (uiState.user != null) onLoginSuccess()
    }

    val backgroundGradient = Brush.verticalGradient(
        listOf(
            Color(0xFFBFE6B8),
            Color(0xFFF1FAF1),
            Color.White
        )
    )

    val infinite = rememberInfiniteTransition(label = "")

    val y by infinite.animateFloat(
        0f, -12f,
        infiniteRepeatable(
            tween(1800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {

        Icon(
            imageVector = Icons.Default.Eco,
            contentDescription = null,
            tint = Color(0x22388E3C),
            modifier = Modifier
                .size(170.dp)
                .align(Alignment.TopStart)
                .offset((-40).dp, (-10).dp)
                .rotate(-20f)
        )

        Icon(
            imageVector = Icons.Default.Eco,
            contentDescription = null,
            tint = Color(0x55388E3C),
            modifier = Modifier
                .size(95.dp)
                .align(Alignment.TopEnd)
                .offset(0.dp, (100 + y).dp)
                .rotate(12f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 56.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(112.dp)
                    .shadow(22.dp, RoundedCornerShape(30.dp))
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF0B2A4A),
                                Color(0xFF123E68)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Color.White.copy(alpha = .3f),
                        RoundedCornerShape(30.dp)
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_oficial),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "iBichos",
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1B5E20)
            )

            Text(
                text = "Caza, colecciona y explora",
                fontSize = 16.sp,
                color = Color(0xFF6A7F6A),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.82f)
                ),
                elevation = CardDefaults.cardElevation(16.dp)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Correo electrónico") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, null)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = greenOutlinedTextFieldColors()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Contraseña") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, null)
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    showPass = !showPass
                                }
                            ) {
                                Icon(
                                    if (showPass)
                                        Icons.Default.VisibilityOff
                                    else
                                        Icons.Default.Visibility,
                                    null
                                )
                            }
                        },
                        visualTransformation =
                            if (showPass)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = greenOutlinedTextFieldColors()
                    )

                    AnimatedVisibility(
                        visible = uiState.error != null
                    ) {
                        Text(
                            text = uiState.error.orEmpty(),
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.login(email, password)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF2E7D32),
                                            Color(0xFF66BB6A)
                                        )
                                    ),
                                    RoundedCornerShape(20.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {

                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else {
                                Text(
                                    "Iniciar Sesión",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            googleLauncher.launch(
                                googleSignInClient.signInIntent
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            "Continuar con Google",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    TextButton(
                        onClick = {
                            showRecoverDialog = true
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            "¿Olvidaste tu contraseña?",
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            TextButton(
                onClick = onNavigateToRegister
            ) {
                Text(
                    "¿No tienes cuenta? Regístrate",
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (showRecoverDialog) {
            AlertDialog(
                onDismissRequest = {
                    showRecoverDialog = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.resetPassword(recoverEmail)
                            showRecoverDialog = false
                        }
                    ) {
                        Text("Enviar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showRecoverDialog = false
                        }
                    ) {
                        Text("Cancelar")
                    }
                },
                title = {
                    Text("Recuperar contraseña")
                },
                text = {
                    Column {
                        Text(
                            "Ingresa tu correo y te enviaremos un enlace."
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = recoverEmail,
                            onValueChange = {
                                recoverEmail = it
                            },
                            placeholder = {
                                Text("Correo electrónico")
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun greenOutlinedTextFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF66BB6A),
        unfocusedBorderColor = Color(0xFFD6D6D6),
        cursorColor = Color(0xFF2E7D32),
        focusedLeadingIconColor = Color(0xFF4CAF50),
        unfocusedLeadingIconColor = Color.Gray,
        focusedTrailingIconColor = Color(0xFF4CAF50),
        unfocusedTrailingIconColor = Color.Gray,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White
    )