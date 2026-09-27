package com.rateel.app.ui.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
private val Light=lightColorScheme(primary=Color(0xFF315B55),secondary=Color(0xFF5C625F),tertiary=Color(0xFF755B35))
private val Dark=darkColorScheme(primary=Color(0xFF9FCFC5),secondary=Color(0xFFC2C8C5),tertiary=Color(0xFFE6C18B))
@Composable fun RateelTheme(dark:Boolean=isSystemInDarkTheme(),content:@Composable()->Unit)=MaterialTheme(colorScheme=if(dark)Dark else Light,typography=Typography(),content=content)
