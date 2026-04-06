package com.medvault.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Teal600,
    onPrimary = Color.White,
    primaryContainer = Teal100,
    onPrimaryContainer = Teal900,
    secondary = Teal700,
    onSecondary = Color.White,
    secondaryContainer = Teal50,
    onSecondaryContainer = Teal800,
    tertiary = Grey600,
    onTertiary = Color.White,
    tertiaryContainer = Grey100,
    onTertiaryContainer = Grey900,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRedContainer,
    onErrorContainer = Color(0xFF7F1D1D),
    background = Color.White,
    onBackground = Grey900,
    surface = Color.White,
    onSurface = Grey900,
    surfaceVariant = Grey50,
    onSurfaceVariant = Grey600,
    outline = Grey300,
    outlineVariant = Grey200,
    surfaceTint = Teal600
)

@Composable
fun MedVaultTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = MedVaultTypography,
        content = content
    )
}
