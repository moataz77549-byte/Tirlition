package com.rateel.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF315B55),
    secondary = Color(0xFF5C625F),
    tertiary = Color(0xFF755B35),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9FCFC5),
    secondary = Color(0xFFC2C8C5),
    tertiary = Color(0xFFE6C18B),
)

@Composable
fun RateelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content,
    )
}
