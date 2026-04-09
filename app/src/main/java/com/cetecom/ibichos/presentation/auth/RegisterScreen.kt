package com.cetecom.ibichos.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cetecom.ibichos.ui.theme.*

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.resetSuccess()
            onRegisterSuccess()
        }
    }

    var name     by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.background))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Text("🦟", fontSize = 48.sp, textAlign = TextAlign.Center)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text       = "Crear Cuenta",
                    style      = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color      = IBichosGreen
                )
                Text(
                    text  = "Únete a la comunidad de cazadores",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Card
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(20.dp),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value         = name,
                        onValueChange = { name = it },
                        label         = { Text("Nombre de cazador") },
                        leadingIcon   = { Icon(Icons.Default.Person, null) },
                        modifier      = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        singleLine    = true,
                        colors        = outlinedTextFieldColors()
                    )
                    OutlinedTextField(
                        value         = email,
                        onValueChange = { email = it },
                        label         = { Text("Correo electrónico") },
                        leadingIcon   = { Icon(Icons.Default.Email, null) },
                        modifier      = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction    = ImeAction.Next
                        ),
                        singleLine = true,
                        colors     = outlinedTextFieldColors()
                    )
                    OutlinedTextField(
                        value         = password,
                        onValueChange = { password = it },
                        label         = { Text("Contraseña") },
                        leadingIcon   = { Icon(Icons.Default.Lock, null) },
                        visualTransformation = if (showPass) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        trailingIcon  = {
                            IconButton(onClick = { showPass = !showPass }) {
                                Icon(
                                    if (showPass) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility, null
                                )
                            }
                        },
                        modifier      = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction    = ImeAction.Done
                        ),
                        singleLine = true,
                        colors     = outlinedTextFieldColors()
                    )

                    AnimatedVisibility(visible = uiState.error != null) {
                        Text(
                            text  = uiState.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(
                        onClick  = { viewModel.register(email, password, name) },
                        enabled  = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = IBichosGreen,
                            contentColor   = OnGreen
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color       = OnGreen
                            )
                        } else {
                            Text(
                                text       = "Crear Cuenta",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 16.sp
                            )
                        }
                    }
                }
            }

            TextButton(onClick = onNavigateBack) {
                Text(
                    text  = "¿Ya tienes cuenta? Inicia sesión",
                    color = IBichosTeal
                )
            }
        }
    }
}

@Composable
private fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = IBichosGreen,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor    = IBichosGreen,
    cursorColor          = IBichosGreen,
    focusedTextColor     = MaterialTheme.colorScheme.onBackground,
    unfocusedTextColor   = MaterialTheme.colorScheme.onBackground,
    focusedLeadingIconColor    = IBichosGreen,
    unfocusedLeadingIconColor  = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedTrailingIconColor   = IBichosGreen,
    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
)

