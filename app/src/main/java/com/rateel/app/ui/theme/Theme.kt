package com.rateel.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.rateel.app.data.settings.ThemeMode

private val LightColors = lightColorScheme(
    primary = Color(0xFF1E5144),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD2E8E0),
    onPrimaryContainer = Color(0xFF00201A),
    secondary = Color(0xFF4A635B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCCE8DF),
    onSecondaryContainer = Color(0xFF062019),
    tertiary = Color(0xFF8F6E31),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF9E0AE),
    onTertiaryContainer = Color(0xFF2A1B00),
    background = Color(0xFFFBF9F5),
    onBackground = Color(0xFF191C1B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191C1B),
    surfaceVariant = Color(0xFFDBE5E0),
    onSurfaceVariant = Color(0xFF3F4946),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF82D5C0),
    onPrimary = Color(0xFF00382E),
    primaryContainer = Color(0xFF005143),
    onPrimaryContainer = Color(0xFF9FF2DC),
    secondary = Color(0xFFB0CCC3),
    onSecondary = Color(0xFF1B352E),
    secondaryContainer = Color(0xFF324B44),
    onSecondaryContainer = Color(0xFFCCE8DF),
    tertiary = Color(0xFFDFC48B),
    onTertiary = Color(0xFF3E2E00),
    tertiaryContainer = Color(0xFF5A440F),
    onTertiaryContainer = Color(0xFFFDE0A4),
    background = Color(0xFF111715),
    onBackground = Color(0xFFE0E3E1),
    surface = Color(0xFF171F1C),
    onSurface = Color(0xFFE0E3E1),
    surfaceVariant = Color(0xFF3F4946),
    onSurfaceVariant = Color(0xFFBFC9C4),
)

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
