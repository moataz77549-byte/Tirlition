package com.rateel.app.ui.theme
import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
private val Light=lightColorScheme(primary=Color(0xFF315B55),secondary=Color(0xFF5C625F),tertiary=Color(0xFF755B35))
private val Dark=darkColorScheme(primary=Color(0xFF9FCFC5),secondary=Color(0xFFC2C8C5),tertiary=Color(0xFFE6C18B))
@Composable fun RateelTheme(dark:Boolean=isSystemInDarkTheme(),content:@Composable()->Unit){val view=LocalView.current;if(!view.isInEditMode)WindowCompat.getInsetsController((view.context as Activity).window,view).isAppearanceLightStatusBars=!dark;MaterialTheme(colorScheme=if(dark)Dark else Light,typography=Typography(),content=content)}
