# EduNav App Background & Styling Setup Guide

## Overview
The EduNav app uses a consistent light blue gradient background across all pages. The app starts with a splash screen that displays the `edu.jpeg` logo while the opening TTS greeting plays.

## Files Created/Updated

### 1. **AppBackground.kt** - Main Background Component
Location: `app/src/main/java/com/example/edunav/ui/theme/AppBackground.kt`

Contains three composable functions:
- **AppBackground()** - Standard light blue gradient background for regular pages
- **AppBackgroundWithImage()** - Background with space for sticker/logo images
- **AppBackgroundWithOverlay()** - Background with semi-transparent overlay for text readability

Color Palette (EduNavColors):
- Primary Light Blue: `#87CEEB` (Sky Blue)
- Dark Light Blue: `#4FA3D1` (Darker Sky Blue)
- Accent Light Blue: `#00A8E8` (Bright Blue)
- Very Light Blue: `#E0F6FF` (Almost white blue)

### 2. **SplashActivity.kt** - Opening Splash Screen
Location: `app/src/main/java/com/example/edunav/SplashActivity.kt`

Features:
- Displays `edu.jpeg` image as a 200dp sticker
- Shows "EduNav" title and tagline
- Plays opening TTS greeting: "Welcome to EduNav. An accessible learning assistant..."
- Automatically navigates to MainActivity after TTS completes
- Shows animated loading indicator
- Respects user's language and voice speed settings from SharedPreferences

### 3. **Theme.kt** - Material Design Theme
Location: `app/src/main/java/com/example/edunav/ui/theme/Theme.kt`

Implements Material 3 color scheme with light blue palette:
- Primary colors map to EduNavColors
- Background color: Very light blue
- Surface colors: Light blue shades
- Text colors optimized for readability

### 4. **Typography.kt** - Text Styling
Location: `app/src/main/java/com/example/edunav/ui/theme/Typography.kt`

Defines typography styles for all text elements:
- Headline styles (Large, Medium, Small)
- Title styles (Large, Medium, Small)
- Body styles (Large, Medium, Small)
- Label styles (Large, Medium, Small)

All text uses dark text color (#1A1A1A) for readability on light backgrounds.

## Image Assets Setup

### Required Image File: edu.jpeg

The app references `R.drawable.edu` (the edu.jpeg image). This file should be placed in:
```
app/src/main/res/drawable/edu.jpeg
```

**Image Requirements:**
- Format: JPEG or PNG
- Recommended size: 400x400 pixels (will be displayed at 200dp)
- Content: Educational logo/sticker (e.g., book icon, graduation cap, etc.)
- Background: Should work with light backgrounds (preferably PNG with transparency)

**If you don't have the image:**
1. Create a 400x400px image with your EduNav logo
2. Save it as `edu.jpeg` in `app/src/main/res/drawable/`
3. The SplashActivity will automatically load it

## Usage in Your Activities

### For Standard Pages (with light blue background):
```kotlin
setContent {
    EduNavTheme {
        AppBackground {
            // Your page content here
            Column {
                Text("Page Title")
                // ... more UI elements
            }
        }
    }
}
```

### For Pages with Background Images:
```kotlin
setContent {
    EduNavTheme {
        AppBackgroundWithImage(
            imageAlignment = Alignment.TopCenter,
            image = {
                Image(
                    painter = painterResource(id = R.drawable.some_image),
                    contentDescription = "Background"
                )
            }
        ) {
            // Your content here
        }
    }
}
```

### For Pages with Text Over Images:
```kotlin
setContent {
    EduNavTheme {
        AppBackgroundWithOverlay(overlayAlpha = 0.5f) {
            // Your content here
        }
    }
}
```

## Access Colors in Your Code

```kotlin
// Use EduNavColors object
val primaryBlue = EduNavColors.PrimaryLightBlue
val darkText = EduNavColors.DarkText
val accentColor = EduNavColors.AccentLightBlue

// In Compose:
Text(
    "Hello",
    color = EduNavColors.LightText,
    fontSize = 24.sp
)
```

## LaunchedActivity Flow

1. **App Launch** → SplashActivity
2. SplashActivity initializes TTS with user's saved preferences
3. Displays edu.jpeg image with app title
4. Speaks opening greeting
5. After TTS completes (or timeout) → MainActivity
6. All pages use AppBackground for consistent styling

## Customization

### Change Gradient Colors:
Edit `AppBackground.kt` - modify the `Brush.verticalGradient()` colors:
```kotlin
colors = listOf(
    Color(0xFF87CEEB),  // Change these hex values
    Color(0xFF4FA3D1)
)
```

### Change Text Colors:
Edit `EduNavColors` object in `AppBackground.kt`:
```kotlin
val DarkText = Color(0xFF1A1A1A)     // Change text color here
val LightText = Color(0xFFFFFFFF)
```

### Change Overlay Alpha:
When using `AppBackgroundWithOverlay()`:
```kotlin
AppBackgroundWithOverlay(overlayAlpha = 0.7f)  // 0f to 1f range
```

## Accessibility Considerations

- **High Contrast**: Light blue background with dark text ensures WCAG AA compliance
- **Readable Fonts**: Typography uses standard sizes for accessibility
- **Image Alt Text**: All images have contentDescription for screen readers
- **Voice Feedback**: SplashActivity speaks greeting while displaying image
- **No Text-Only**: Critical info not presented as image-only

## Testing

1. Build and run the app
2. SplashActivity should display with edu.jpeg and speak greeting
3. After ~4-5 seconds, automatically navigate to MainActivity
4. All pages should have light blue gradient background
5. Text should be clearly readable (dark text on light background)

## Troubleshooting

**Image not showing:**
- Ensure edu.jpeg exists in `app/src/main/res/drawable/`
- Check R.drawable.edu references are correct
- Rebuild project with `./gradlew clean build`

**Colors not appearing:**
- Verify Theme.kt is properly imported in your Activities
- Make sure you're using EduNavTheme wrapper
- Check AppBackground is wrapping your content

**TTS not playing during splash:**
- Verify TextToSpeech initialization in onInit()
- Check Android permissions: android.permission.RECORD_AUDIO
- Test on device with TTS engine installed

## Files Modified/Created

✅ AppBackground.kt - NEW
✅ SplashActivity.kt - NEW  
✅ Theme.kt - NEW
✅ Typography.kt - NEW
✅ AndroidManifest.xml - Already has SplashActivity as launcher
