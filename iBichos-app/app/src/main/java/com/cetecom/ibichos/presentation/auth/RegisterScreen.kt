package com.cetecom.ibichos.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.cetecom.ibichos.R
import com.cetecom.ibichos.domain.model.ChileanData
import com.cetecom.ibichos.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val DarkGreen = Color(0xFF0F3D2E)
private val MediumGreen = Color(0xFF1F7A5A)
private val SoftGreen = Color(0xFFEAF8EF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val onRegisterSuccessUpdated by rememberUpdatedState(onRegisterSuccess)
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.resetSuccess()
            onRegisterSuccessUpdated()
        }
    }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showPass by remember { mutableStateOf(false) }
    var showConfirmPass by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    var region by remember { mutableStateOf("") }
    var regionExpanded by remember { mutableStateOf(false) }

    var comuna by remember { mutableStateOf("") }
    var comunaExpanded by remember { mutableStateOf(false) }

    var gender by remember { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }

    var birthDate by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

    val defaultBirthMillis = remember {
        java.util.Calendar.getInstance().apply { add(java.util.Calendar.YEAR, -20) }.timeInMillis
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = defaultBirthMillis,
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

    val locations by viewModel.locations.collectAsStateWithLifecycle()
    val comunasForRegion = remember(region, locations) {
        locations[region] ?: emptyList()
    }

    val canRegister =
        name.isNotBlank() &&
                email.isNotBlank() &&
                region.isNotBlank() &&
                comuna.isNotBlank() &&
                birthDate.isNotBlank() &&
                gender.isNotBlank() &&
                password.isNotBlank() &&
                confirmPassword.isNotBlank() &&
                password == confirmPassword &&
                nameError.isEmpty() &&
                emailError.isEmpty() &&
                passwordError.isEmpty() &&
                confirmPasswordError.isEmpty() &&
                !uiState.isLoading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        DarkGreen,
                        MediumGreen,
                        SoftGreen
                    )
                )
            )
    ) {
        AnimatedLeavesBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(34.dp))

            Image(
                painter = painterResource(id = R.drawable.logo_oficial),
                contentDescription = "Logo iBichos",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Crear Cuenta",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = "Únete a la comunidad de cazadores",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 18.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = DarkGreen.copy(alpha = 0.28f)
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.96f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            val (isValid, error) = validateName(it)
                            nameError = if (isValid) "" else error
                        },
                        label = { Text("Nombre de cazador") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        isError = nameError.isNotEmpty(),
                        supportingText = { if (nameError.isNotEmpty()) Text(nameError) },
                        colors = outlinedTextFieldColors()
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            val (isValid, error) = validationEmail(it)
                            emailError = if (isValid) "" else error
                        },
                        label = { Text("Correo electrónico") },
                        leadingIcon = { Icon(Icons.Default.Email, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        isError = emailError.isNotEmpty(),
                        supportingText = { if (emailError.isNotEmpty()) Text(emailError) },
                        colors = outlinedTextFieldColors()
                    )

                    ExposedDropdownMenuBox(
                        expanded = regionExpanded,
                        onExpandedChange = { regionExpanded = !regionExpanded }
                    ) {
                        OutlinedTextField(
                            value = region,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Región") },
                            leadingIcon = { Icon(Icons.Default.Place, null) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(regionExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(16.dp),
                            colors = outlinedTextFieldColors()
                        )

                        ExposedDropdownMenu(
                            expanded = regionExpanded,
                            onDismissRequest = { regionExpanded = false }
                        ) {
                            locations.keys.forEach { r ->
                                DropdownMenuItem(
                                    text = { Text(r) },
                                    onClick = {
                                        region = r
                                        comuna = ""
                                        regionExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = comunaExpanded,
                        onExpandedChange = {
                            if (region.isNotEmpty()) comunaExpanded = !comunaExpanded
                        }
                    ) {
                        OutlinedTextField(
                            value = comuna,
                            onValueChange = {},
                            readOnly = true,
                            enabled = region.isNotEmpty(),
                            label = { Text("Comuna") },
                            leadingIcon = { Icon(Icons.Default.Home, null) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(comunaExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(16.dp),
                            colors = outlinedTextFieldColors()
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
                        shape = RoundedCornerShape(16.dp),
                        colors = outlinedTextFieldColors(),
                        enabled = false
                    )

                    ExposedDropdownMenuBox(
                        expanded = genderExpanded,
                        onExpandedChange = { genderExpanded = !genderExpanded }
                    ) {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Sexo") },
                            leadingIcon = { Icon(Icons.Default.Wc, null) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(genderExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(16.dp),
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
                        value = password,
                        onValueChange = {
                            password = it
                            val (isValid, error) = validatePassword(it)
                            passwordError = if (isValid) "" else error

                            confirmPasswordError =
                                if (confirmPassword.isNotEmpty() && confirmPassword != password) {
                                    "Las contraseñas no coinciden"
                                } else ""
                        },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { showPass = !showPass }) {
                                Icon(
                                    if (showPass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    null
                                )
                            }
                        },
                        visualTransformation = if (showPass) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        isError = passwordError.isNotEmpty(),
                        supportingText = { if (passwordError.isNotEmpty()) Text(passwordError) },
                        colors = outlinedTextFieldColors()
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            confirmPasswordError =
                                if (it != password) "Las contraseñas no coinciden" else ""
                        },
                        label = { Text("Confirmar contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { showConfirmPass = !showConfirmPass }) {
                                Icon(
                                    if (showConfirmPass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    null
                                )
                            }
                        },
                        visualTransformation = if (showConfirmPass) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        isError = confirmPasswordError.isNotEmpty(),
                        supportingText = {
                            if (confirmPasswordError.isNotEmpty()) Text(confirmPasswordError)
                        },
                        colors = outlinedTextFieldColors()
                    )

                    AnimatedVisibility(visible = uiState.error != null) {
                        Text(
                            text = uiState.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(
                        onClick = {
                            val genderEnum = when (gender) {
                                "Hombre" -> "MALE"
                                "Mujer" -> "FEMALE"
                                "Otro" -> "OTHER"
                                "Prefiero no decirlo" -> "PREFER_NOT_TO_SAY"
                                else -> "UNSPECIFIED"
                            }

                            viewModel.register(
                                email = email,
                                password = password,
                                displayName = name,
                                region = region,
                                city = comuna,
                                birthDate = birthDate,
                                gender = genderEnum
                            )
                        },
                        enabled = canRegister,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkGreen,
                            contentColor = Color.White,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Icon(Icons.Default.Eco, contentDescription = null)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Crear Cuenta",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            TextButton(onClick = onNavigateBack) {
                Text(
                    text = "¿Ya tienes una cuenta? Iniciar sesión",
                    color = DarkGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            birthDate = sdf.format(Date(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar", color = DarkGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = DarkGreen)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun AnimatedLeavesBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "leaves")

    val offsetOne by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 70f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetOne"
    )

    val offsetTwo by infiniteTransition.animateFloat(
        initialValue = 60f,
        targetValue = -40f,
        animationSpec = infiniteRepeatable(
            animation = tween(7500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetTwo"
    )

    val alphaLeaves by infiniteTransition.animateFloat(
        initialValue = 0.06f,
        targetValue = 0.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alphaLeaves"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Icon(
            imageVector = Icons.Default.Eco,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(150.dp)
                .alpha(alphaLeaves)
                .offset(x = (-40).dp, y = offsetOne.dp)
        )

        Icon(
            imageVector = Icons.Default.Eco,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(110.dp)
                .alpha(alphaLeaves)
                .offset(x = 28.dp, y = offsetTwo.dp)
        )

        Icon(
            imageVector = Icons.Default.Eco,
            contentDescription = null,
            tint = DarkGreen,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(180.dp)
                .alpha(alphaLeaves)
                .offset(x = (-45).dp, y = 20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = DarkGreen,
    unfocusedBorderColor = MediumGreen.copy(alpha = 0.35f),
    disabledBorderColor = MediumGreen.copy(alpha = 0.25f),

    focusedLabelColor = DarkGreen,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,

    cursorColor = DarkGreen,

    focusedLeadingIconColor = DarkGreen,
    unfocusedLeadingIconColor = MediumGreen,
    disabledLeadingIconColor = MediumGreen.copy(alpha = 0.55f),

    focusedTrailingIconColor = DarkGreen,
    unfocusedTrailingIconColor = MediumGreen,

    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    disabledContainerColor = Color.White,

    focusedTextColor = MaterialTheme.colorScheme.onBackground,
    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
    disabledTextColor = MaterialTheme.colorScheme.onBackground
)

