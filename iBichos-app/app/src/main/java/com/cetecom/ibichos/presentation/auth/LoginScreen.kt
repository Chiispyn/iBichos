
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
    ) { result ->
        viewModel.handleGoogleResult(result)
    }

    LaunchedEffect(uiState.user) {
        if (uiState.user != null) {
            onLoginSuccess()
        }
    }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFBFE6B8),
            Color(0xFFF1FAF1),
            Color(0xFFFFFFFF)
        )
    )

    val infiniteTransition = rememberInfiniteTransition(label = "")

    val y1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    val r1 by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    val y2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    val r2 by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    val y3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    val r3 by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    val y4 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -13f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    val r4 by infiniteTransition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
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
                .offset(x = (-40).dp, y = (-10).dp)
                .rotate(-25f)
        )

        Icon(
            imageVector = Icons.Default.Eco,
            contentDescription = "Hoja",
            tint = Color(0xFF66BB6A),
            modifier = Modifier
                .size(90.dp)
                .align(Alignment.TopEnd)
                .offset(x = 10.dp, y = (90 + y1).dp)
                .rotate(r1)
        )

        Icon(
            imageVector = Icons.Default.Eco,
            contentDescription = null,
            tint = Color(0x55388E3C),
            modifier = Modifier
                .size(70.dp)
                .align(Alignment.CenterStart)
                .offset(x = (-20).dp, y = y2.dp)
                .rotate(r2)
                .alpha(0.7f)
        )

        Icon(
            imageVector = Icons.Default.Eco,
            contentDescription = null,
            tint = Color(0x33388E3C),
            modifier = Modifier
                .size(110.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-10).dp, y = (-40 + y3).dp)
                .rotate(r3)
                .alpha(0.65f)
        )

        Icon(
            imageVector = Icons.Default.Eco,
            contentDescription = null,
            tint = Color(0x443FA34D),
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.TopCenter)
                .offset(x = 40.dp, y = (120 + y4).dp)
                .rotate(r4)
                .alpha(0.75f)
        )

        Icon(
            imageVector = Icons.Default.Eco,
            contentDescription = null,
            tint = Color(0x14388E3C),
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-30).dp, y = (-20).dp)
                .rotate(30f)
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
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(30.dp),
                        ambientColor = Color(0x332E7D32),
                        spotColor = Color(0x332E7D32)
                    )
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF0B2A4A),
                                Color(0xFF123E68)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.30f),
                        shape = RoundedCornerShape(30.dp)
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_oficial),
                    contentDescription = "Logo iBichos",
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

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Caza, colecciona y explora",
                fontSize = 16.sp,
                color = Color(0xFF6A7F6A),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(30.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.78f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Correo electrónico") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        colors = greenOutlinedTextFieldColors()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Contraseña") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null
                            )
                        },
                        visualTransformation = if (showPass) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPass = !showPass }) {
                                Icon(
                                    imageVector = if (showPass) {
                                        Icons.Default.VisibilityOff
                                    } else {
                                        Icons.Default.Visibility
                                    },
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        colors = greenOutlinedTextFieldColors()
                    )

                    AnimatedVisibility(visible = uiState.error != null) {
                        Text(
                            text = uiState.error.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { viewModel.login(email, password) },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF2E7D32),
                                            Color(0xFF66BB6A)
                                        )
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = "Iniciar Sesión",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            googleLauncher.launch(googleSignInClient.signInIntent)
                        },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF212121)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "G",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF4285F4)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Continuar con Google",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            TextButton(onClick = onNavigateToRegister) {
                Text(
                    text = "¿No tienes cuenta? Regístrate",
                    color = Color(0xFF2E7D32),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun greenOutlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF66BB6A),
    unfocusedBorderColor = Color(0xFFD6D6D6),
    focusedPlaceholderColor = Color(0xFF8A8A8A),
    unfocusedPlaceholderColor = Color(0xFF9E9E9E),
    cursorColor = Color(0xFF2E7D32),
    focusedTextColor = Color(0xFF212121),
    unfocusedTextColor = Color(0xFF212121),
    focusedLeadingIconColor = Color(0xFF4CAF50),
    unfocusedLeadingIconColor = Color(0xFF7A7A7A),
    focusedTrailingIconColor = Color(0xFF4CAF50),
    unfocusedTrailingIconColor = Color(0xFF7A7A7A),
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        onLoginSuccess = {},
        onNavigateToRegister = {}
    )
}