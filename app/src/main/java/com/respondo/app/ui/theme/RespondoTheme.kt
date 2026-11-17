package com.respondo.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RespondoColors = darkColorScheme(
    primary = Color(0xFF8CE0C0),
    onPrimary = Color(0xFF073B2B),
    background = Color(0xFF0D1513),
    surface = Color(0xFF15201D),
    surfaceVariant = Color(0xFF202C28),
    onBackground = Color(0xFFEAF4EF),
    onSurface = Color(0xFFEAF4EF),
    onSurfaceVariant = Color(0xFFA5B5AE),
)

@Composable
fun RespondoTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = RespondoColors, content = content)
}
