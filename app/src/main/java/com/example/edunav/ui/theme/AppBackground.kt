package com.example.edunav.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Light blue color palette for EduNav app.
 * Provides consistent styling across all pages.
 */
object EduNavColors {
    // Primary light blue shades
    val PrimaryLightBlue = Color(0xFF87CEEB)      // Sky blue
    val DarkLightBlue = Color(0xFF4FA3D1)         // Darker sky blue
    val AccentLightBlue = Color(0xFF00A8E8)       // Bright blue
    val VeryLightBlue = Color(0xFFE0F6FF)         // Very light blue (almost white)
    
    // Background gradient
    val GradientStartBlue = Color(0xFF87CEEB)     // Sky blue start
    val GradientEndBlue = Color(0xFF4FA3D1)       // Darker blue end
    
    // Supporting colors
    val DarkText = Color(0xFF1A1A1A)              // Dark text
    val LightText = Color(0xFFFFFFFF)             // White text
    val AccentGold = Color(0xFFFFD700)            // Gold for highlights
    val ErrorRed = Color(0xFFE74C3C)              // Error color
    val SuccessGreen = Color(0xFF27AE60)          // Success color
}

/**
 * AppBackground composable wraps content with light blue gradient background.
 * This ensures consistent styling across all pages in the app.
 *
 * @param content The composable content to display on top of the background
 */
@Composable
fun AppBackground(content: @Composable () -> Unit) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            EduNavColors.GradientStartBlue,  // Sky blue at top
            EduNavColors.GradientEndBlue     // Darker blue at bottom
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBrush),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent
        ) {
            content()
        }
    }
}

/**
 * AppBackgroundWithImage composable includes a sticker/watermark image.
 * Used for splash screens and opening pages where the app logo/image is displayed.
 *
 * @param imageModifier Modifier for the image
 * @param imageAlignment Alignment of the image on the screen
 * @param image The composable image to display
 * @param content The main content to display over the background
 */
@Composable
fun AppBackgroundWithImage(
    imageModifier: Modifier = Modifier,
    imageAlignment: Alignment = Alignment.Center,
    image: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            EduNavColors.GradientStartBlue,
            EduNavColors.GradientEndBlue
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBrush)
    ) {
        // Background image/sticker
        Box(
            modifier = imageModifier.align(imageAlignment)
        ) {
            image()
        }

        // Main content
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent
        ) {
            content()
        }
    }
}

/**
 * Overlay background with semi-transparent overlay for better text readability.
 * Used when displaying text over images.
 *
 * @param overlayAlpha Alpha value of the overlay (0f to 1f)
 * @param overlayColor Color of the overlay
 * @param content The composable content to display
 */
@Composable
fun AppBackgroundWithOverlay(
    overlayAlpha: Float = 0.3f,
    overlayColor: Color = Color.Black,
    content: @Composable () -> Unit
) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            EduNavColors.GradientStartBlue,
            EduNavColors.GradientEndBlue
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBrush)
    ) {
        // Semi-transparent overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(overlayColor.copy(alpha = overlayAlpha))
        )

        // Main content
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent
        ) {
            content()
        }
    }
}
