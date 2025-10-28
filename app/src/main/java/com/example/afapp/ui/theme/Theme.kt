package com.example.afapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- Esquema Oscuro (Modo Asfalto) ---
private val DarkColorScheme = darkColorScheme(
    primary = CamelAccent,
    onPrimary = AsfaltoBlack, // Texto oscuro sobre el acento camel
    primaryContainer = CamelAccent.copy(alpha = 0.2f),
    onPrimaryContainer = TextLight,

    secondary = UrbanGray,
    onSecondary = TextLight,
    secondaryContainer = UrbanGray.copy(alpha = 0.5f),
    onSecondaryContainer = TextLight,

    background = AsfaltoBlack, // Fondo carbón
    onBackground = TextLight,  // Texto claro
    surface = AsfaltoBlack,
    onSurface = TextLight,
    surfaceVariant = UrbanGray,
    onSurfaceVariant = TextLight,

    error = AppError,
    onError = AlabasterWhite,

    tertiary = CamelAccent,
    onTertiary = AsfaltoBlack
)

// --- Esquema Claro (Modo Alabastro) ---
private val LightColorScheme = lightColorScheme(
    primary = CamelAccent,
    onPrimary = AsfaltoBlack, // Texto oscuro sobre el acento camel
    primaryContainer = CamelAccent.copy(alpha = 0.2f),
    onPrimaryContainer = TextDark,

    secondary = UrbanGray,
    onSecondary = TextDark,
    secondaryContainer = UrbanGray.copy(alpha = 0.2f),
    onSecondaryContainer = TextDark,

    background = AlabasterWhite, // Fondo blanco roto
    onBackground = TextDark,     // Texto oscuro
    surface = AlabasterWhite,
    onSurface = TextDark,
    surfaceVariant = UrbanGray.copy(alpha = 0.1f), // Un gris más claro para variantes
    onSurfaceVariant = TextDark,

    error = AppError,
    onError = AlabasterWhite,

    tertiary = CamelAccent,
    onTertiary = AsfaltoBlack
)


@Composable
fun afappAppTheme( // ⬅️ NOMBRE: afappAppTheme
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Usa el archivo Typography.kt
        content = content
    )
}