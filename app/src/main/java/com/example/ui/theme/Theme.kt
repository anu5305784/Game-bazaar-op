package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = Color.White,
    primaryContainer = NeonPurpleDark,
    onPrimaryContainer = Color.White,
    secondary = CyberCyan,
    onSecondary = Color.Black,
    secondaryContainer = ElectricBlue,
    onSecondaryContainer = Color.White,
    tertiary = GoldLegendary,
    background = GamerBg,
    onBackground = GamerTextPrimary,
    surface = GamerSurface,
    onSurface = GamerTextPrimary,
    surfaceVariant = GamerSurfaceElevated,
    onSurfaceVariant = GamerTextSecondary,
    outline = GamerCardBorder,
    error = CrimsonAlert,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Gamer Bazaar defaults to premium dark gaming theme
    dynamicColor: Boolean = false, // Preserve our distinctive esports neon aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
