package com.example.meinstundenzhler.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF00C2A8),
    onPrimary = Color(0xFF003730),
    primaryContainer = Color(0xFF005247),
    onPrimaryContainer = Color(0xFF7AF8E2),

    secondary = Color(0xFF8BD0C7),
    onSecondary = Color(0xFF003732),
    secondaryContainer = Color(0xFF004F48),
    onSecondaryContainer = Color(0xFFAEECE3),

    background = Color(0xFF0B0F10),
    onBackground = Color(0xFFE1E3E5),
    surface = Color(0xFF121417),
    onSurface = Color(0xFFE1E3E5),
    surfaceVariant = Color(0xFF2B3136),
    onSurfaceVariant = Color(0xFFC3C7CD),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun MeinStundenzählerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, typography = Typography, content = content)
}
