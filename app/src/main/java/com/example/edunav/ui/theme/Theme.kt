package com.example.edunav.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightBlueColorScheme = lightColorScheme(
    primary = EduNavColors.DarkLightBlue,
    onPrimary = EduNavColors.LightText,
    primaryContainer = EduNavColors.PrimaryLightBlue,
    onPrimaryContainer = EduNavColors.DarkText,
    secondary = EduNavColors.AccentLightBlue,
    onSecondary = EduNavColors.LightText,
    secondaryContainer = EduNavColors.VeryLightBlue,
    onSecondaryContainer = EduNavColors.DarkText,
    tertiary = EduNavColors.AccentGold,
    onTertiary = EduNavColors.DarkText,
    background = EduNavColors.VeryLightBlue,
    onBackground = EduNavColors.DarkText,
    surface = EduNavColors.PrimaryLightBlue,
    onSurface = EduNavColors.LightText,
    error = EduNavColors.ErrorRed,
    onError = Color.White
)

@Composable
fun EduNavTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightBlueColorScheme,
        typography = Typography,
        content = content
    )
}
