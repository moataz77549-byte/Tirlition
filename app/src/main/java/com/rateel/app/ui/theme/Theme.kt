package com.rateel.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rateel.app.data.settings.ThemeMode

private val LightColors = lightColorScheme(
    primary = Color(0xFF145C55),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1EDE6),
    onPrimaryContainer = Color(0xFF073B36),
    secondary = Color(0xFF58635F),
    secondaryContainer = Color(0xFFDEEAE4),
    tertiary = Color(0xFF80602C),
    tertiaryContainer = Color(0xFFF9E2B2),
    background = Color(0xFFFAF9F5),
    surface = Color(0xFFFAF9F5),
    surfaceVariant = Color(0xFFE6EAE5),
    onSurface = Color(0xFF1A2421),
    outlineVariant = Color(0xFFC1CAC4),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9FCFC5),
    secondary = Color(0xFFC2C8C5),
    tertiary = Color(0xFFE6C18B),
    background = Color(0xFF111A19),
    surface = Color(0xFF111A19),
    surfaceVariant = Color(0xFF394743),
    onSurface = Color(0xFFE0EAE5),
    primaryContainer = Color(0xFF174C46),
    onPrimaryContainer = Color(0xFFD1EDE6),
    outlineVariant = Color(0xFF414F4A),
)

object RateelSpacing {
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
}

@Composable
fun RateelTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content,
    )
}
