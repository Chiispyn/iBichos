package com.cetecom.ibichos.presentation.auth

import android.util.Patterns
import kotlin.text.any
import kotlin.text.isDigit
import kotlin.text.isEmpty
import kotlin.text.isUpperCase
import kotlin.text.matches

fun validationEmail(email: String): Pair<Boolean, String> {
    return when {
        email.isEmpty() ->
            Pair(false, "El correo es requerido")

        !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
            Pair(false, "Correo inválido")

        !email.matches(Regex("^[A-Za-z0-9._%+-]+@(gmail|hotmail|yahoo)\\.(com|cl)$")) ->
            Pair(false, "Solo se permiten correos Gmail, Hotmail o Yahoo")

        else ->
            Pair(true, "")
    }
}

fun validatePassword(password: String): Pair<Boolean, String> {
    return when {
        password.isEmpty() ->
            Pair(false, "La contraseña es requerida")

        password.length < 8 ->
            Pair(false, "La contraseña debe tener al menos 8 caracteres")

        !password.any { it.isDigit() } ->
            Pair(false, "La contraseña debe tener al menos un número")

        !password.any { it.isUpperCase() } ->
            Pair(false, "La contraseña debe tener al menos una letra mayúscula")

        else ->
            Pair(true, "")
    }
}

fun validateName(name: String): Pair<Boolean, String> {
    return when {
        name.isEmpty() ->
            Pair(false, "El nombre es requerido")

        name.length < 3 ->
            Pair(false, "El nombre debe tener al menos 3 caracteres")

        !name.matches(Regex("^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ ]+$")) ->
            Pair(false, "Solo se permiten letras y números")

        name.matches(Regex("^[0-9]+$")) ->
            Pair(false, "El nombre no puede ser solo números")

        else ->
            Pair(true, "")
    }
}

fun validateConfirmPassword(password: String, confirmPassword: String): Pair<Boolean, String> {
    return when {
        confirmPassword.isEmpty() -> Pair(false, "La confirmación es requerida")
        confirmPassword.length < 8 -> Pair(false, "La contraseña debe tener al menos 8 caracteres")
        confirmPassword != password -> Pair(false, "Las contraseñas no coinciden")
        else -> Pair(true, "")
    }
}
