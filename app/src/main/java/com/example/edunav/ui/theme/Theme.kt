package com.example.edunav.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val Blue200 = Color(0xFF2196F3)
private val Blue500 = Color(0xFF1976D2)
private val Blue700 = Color(0xFF0D47A1)

private val LightColors = lightColorScheme(
    primary = Blue500,
    secondary = Blue700,
    tertiary = Blue200
)

private val DarkColors = darkColorScheme(
    primary = Blue500,
    secondary = Blue700,
    tertiary = Blue200
)

private val AppTypography = Typography()
private val AppShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp)
)

@Composable
fun EduNavTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}