package com.cetecom.ibichos.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cetecom.ibichos.R
import com.cetecom.ibichos.ui.theme.*
import com.cetecom.ibichos.domain.model.ChileanData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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

    // Nuevos campos
    var region by remember { mutableStateOf("") }
    var regionExpanded by remember { mutableStateOf(false) }
    
    var comuna by remember { mutableStateOf("") }
    var comunaExpanded by remember { mutableStateOf(false) }
    
    var gender by remember { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }

    var birthDate by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val datePickerState = rememberDatePickerState(
        yearRange = IntRange(currentYear - 100, currentYear),
        selectableDates = object : SelectableDates {
            override fun isSelectableYear(year: Int): Boolean {
                return year >= currentYear - 100 && year <= currentYear
            }
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= System.currentTimeMillis()
            }
        }
    )
    
    val scrollState = rememberScrollState()

    // Opciones dependientes
    val comunasForRegion = remember(region) {
        ChileanData.regionsWithComunas[region] ?: emptyList()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(IBichosGreen.copy(alpha = 0.3f), MaterialTheme.colorScheme.background)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            // Header
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.logo_oficial),
                contentDescription = "Logo iBichos",
                modifier = Modifier.size(180.dp)
            )
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

                    // REGION Dropdown
                    ExposedDropdownMenuBox(
                        expanded = regionExpanded,
                        onExpandedChange = { regionExpanded = !regionExpanded }
                    ) {
                        OutlinedTextField(
                            value = region,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Región") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = regionExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = regionExpanded,
                            onDismissRequest = { regionExpanded = false }
                        ) {
                            ChileanData.regionsWithComunas.keys.forEach { r ->
                                DropdownMenuItem(
                                    text = { Text(r) },
                                    onClick = {
                                        region = r
                                        comuna = "" // reset comuna
                                        regionExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // COMUNA Dropdown
                    ExposedDropdownMenuBox(
                        expanded = comunaExpanded,
                        onExpandedChange = { if (region.isNotEmpty()) comunaExpanded = !comunaExpanded }
                    ) {
                        OutlinedTextField(
                            value = comuna,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Comuna") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = comunaExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = outlinedTextFieldColors(),
                            enabled = region.isNotEmpty()
                        )
                        ExposedDropdownMenu(
                            expanded = comunaExpanded,
                            onDismissRequest = { comunaExpanded = false }
                        ) {
                            comunasForRegion.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c) },
                                    onClick = {
                                        comuna = c
                                        comunaExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // FECHA DE NACIMIENTO
                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha de nacimiento") },
                        leadingIcon = { Icon(Icons.Default.DateRange, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showDatePicker = true },
                        colors = outlinedTextFieldColors(),
                        enabled = false // To capture clicks via modifier more reliably
                    )

                    if (showDatePicker) {
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                        // Sumar zone offset si es necesario, pero como es solo dd/MM/yyyy suele estar bien
                                        birthDate = sdf.format(Date(millis))
                                    }
                                    showDatePicker = false
                                }) { Text("Aceptar", color = IBichosGreen) }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = IBichosGreen) }
                            }
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }

                    // SEXO Dropdown
                    ExposedDropdownMenuBox(
                        expanded = genderExpanded,
                        onExpandedChange = { genderExpanded = !genderExpanded }
                    ) {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Sexo") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = genderExpanded,
                            onDismissRequest = { genderExpanded = false }
                        ) {
                            ChileanData.genders.forEach { g ->
                                DropdownMenuItem(
                                    text = { Text(g) },
                                    onClick = {
                                        gender = g
                                        genderExpanded = false
                                    }
                                )
                            }
                        }
                    }

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
                        onClick  = { viewModel.register(email, password, name, region, comuna, birthDate, gender) },
                        enabled  = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = IBichosGreen,
                            contentColor   = OnGreen,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
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
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledBorderColor  = MaterialTheme.colorScheme.outline,
    disabledTextColor    = MaterialTheme.colorScheme.onBackground,
    disabledLabelColor   = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
)
