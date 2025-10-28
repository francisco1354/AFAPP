package com.example.afapp.domain.validation

import android.util.Patterns // Usamos el patrón estándar de Android para emails

// Valida que el email no esté vacío y cumpla patrón de email
fun validateEmail(email: String): String? {                            // Retorna String? (mensaje) o null si está OK
    if (email.isBlank()) return "El email es obligatorio"              // Regla 1: no vacío
    val ok = Patterns.EMAIL_ADDRESS.matcher(email).matches()           // Regla 2: coincide con patrón de email
    return if (!ok) "Formato de email inválido" else null              // Si no cumple, devolvemos mensaje
}

// Valida que el nombre contenga solo letras y espacios (sin números)
fun validateNameLettersOnly(name: String): String? {                   // Valida nombre
    if (name.isBlank()) return "El nombre es obligatorio"              // Regla 1: no vacío
    val regex = Regex("^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$")                      // Regla 2: solo letras y espacios (con tildes/ñ)
    return if (!regex.matches(name)) "Solo letras y espacios" else null// Mensaje si falla
}

// Valida que el teléfono tenga solo dígitos y una longitud razonable
fun validatePhoneDigitsOnly(phone: String): String? {                  // Valida teléfono
    if (phone.isBlank()) return "El teléfono es obligatorio"           // Regla 1: no vacío
    if (!phone.all { it.isDigit() }) return "Solo números"             // Regla 2: todos dígitos
    if (phone.length !in 8..15) return "Debe tener entre 8 y 15 dígitos" // Regla 3: tamaño razonable
    return null                                                        // OK
}

// Valida seguridad de la contraseña (mín. 8, mayús, minús, número y símbolo; sin espacios)
fun validateStrongPassword(pass: String): String? {                    // Requisitos mínimos de seguridad
    if (pass.isBlank()) return "La contraseña es obligatoria"          // No vacío
    if (pass.length < 8) return "Mínimo 8 caracteres"                  // Largo mínimo
    if (!pass.any { it.isUpperCase() }) return "Debe incluir una mayúscula" // Al menos 1 mayúscula
    if (!pass.any { it.isLowerCase() }) return "Debe incluir una minúscula" // Al menos 1 minúscula
    if (!pass.any { it.isDigit() }) return "Debe incluir un número"         // Al menos 1 número
    if (!pass.any { !it.isLetterOrDigit() }) return "Debe incluir un símbolo" // Al menos 1 símbolo
    if (pass.contains(' ')) return "No debe contener espacios"          // Sin espacios
    return null                                                         // OK
}

// Valida que la confirmación coincida con la contraseña
fun validateConfirm(pass: String, confirm: String): String? {          // Confirmación de contraseña
    if (confirm.isBlank()) return "Confirma tu contraseña"             // No vacío
    return if (pass != confirm) "Las contraseñas no coinciden" else null // Deben ser iguales
}
