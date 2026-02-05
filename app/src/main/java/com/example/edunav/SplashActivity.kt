package com.example.edunav

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edunav.ui.theme.AppBackground
import com.example.edunav.ui.theme.EduNavColors
import com.example.edunav.ui.theme.EduNavTheme
import java.util.*

/**
 * SplashActivity displays the app logo/opening image (edu.jpeg) with an initial TTS greeting.
 * This is the first screen users see when launching EduNav.
 * 
 * Features:
 * - Displays edu.jpeg logo image
 * - Speaks a welcome greeting using TTS
 * - Shows progress indicator
 * - Provides skip button for accessibility
 * - Auto-navigates to MainActivity after TTS completes or timeout
 * - Handles TTS failures gracefully
 */
class SplashActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var isReadyToNavigate = false
    private val APP_VERSION = "1.0.0"
    private val MAX_SPLASH_DURATION = 6000L  // 6 seconds max
    private var hasNavigated = false
    private var ttsStarted = false

    companion object {
        private const val TAG = "SplashActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        // Set timeout to ensure app doesn't stay on splash forever
        Handler(Looper.getMainLooper()).postDelayed({
            if (!hasNavigated) {
                navigateToHome()
            }
        }, MAX_SPLASH_DURATION)

        setContent {
            EduNavTheme {
                SplashScreen(
                    isReadyToNavigate = isReadyToNavigate,
                    appVersion = APP_VERSION,
                    ttsStarted = ttsStarted,
                    onNavigateToHome = { navigateToHome() },
                    onSkip = { navigateToHome() }
                )
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val prefs = getSharedPreferences("EduNavPrefs", Context.MODE_PRIVATE)
            val langCode = prefs.getString("language", "en") ?: "en"
            val speed = prefs.getFloat("voiceSpeed", 1.0f)
            val pitch = prefs.getFloat("voicePitch", 1.0f)

            tts.language = Locale(langCode)
            tts.setSpeechRate(speed)
            tts.setPitch(pitch)

            // Set up utterance progress listener
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    ttsStarted = true
                    android.util.Log.d(TAG, "TTS started: $utteranceId")
                }

                override fun onDone(utteranceId: String?) {
                    android.util.Log.d(TAG, "TTS completed: $utteranceId")
                    if (utteranceId == "OPENING") {
                        Handler(Looper.getMainLooper()).postDelayed({
                            isReadyToNavigate = true
                            navigateToHome()
                        }, 1000)
                    }
                }

                override fun onError(utteranceId: String?) {
                    android.util.Log.e(TAG, "TTS error: $utteranceId")
                    if (!hasNavigated) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            navigateToHome()
                        }, 1500)
                    }
                }
            })

            speakOpening()
        } else {
            // TTS initialization failed
            Handler(Looper.getMainLooper()).postDelayed({
                if (!hasNavigated) navigateToHome()
            }, 2000)
        }
    }

    private fun speakOpening() {
        val greeting = "Welcome to EduNav. An accessible learning assistant for blind and visually impaired students. " +
                "Initializing the app. Please wait."
        tts.speak(greeting, TextToSpeech.QUEUE_FLUSH, null, "OPENING")
    }

    private fun navigateToHome() {
        if (hasNavigated) return  // Prevent multiple navigations
        hasNavigated = true

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}

@Composable
fun SplashScreen(
    isReadyToNavigate: Boolean,
    appVersion: String,
    ttsStarted: Boolean,
    onNavigateToHome: () -> Unit,
    onSkip: () -> Unit
) {
    var progress by remember { mutableStateOf(0f) }
    var elapsedTime by remember { mutableStateOf(0L) }
    val startTime = remember { System.currentTimeMillis() }
    val maxDuration = 6000L  // 6 seconds

    // Update progress bar
    LaunchedEffect(Unit) {
        while (elapsedTime < maxDuration) {
            android.os.SystemClock.sleep(100)
            elapsedTime = System.currentTimeMillis() - startTime
            progress = (elapsedTime.toFloat() / maxDuration).coerceIn(0f, 1f)
        }
    }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    // Tap anywhere to navigate (accessibility feature)
                    onNavigateToHome()
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Top section with logo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                // Display edu.jpeg image as a sticker
                Image(
                    painter = painterResource(id = R.drawable.edu),
                    contentDescription = "EduNav Logo",
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    "EduNav",
                    fontSize = 48.sp,
                    color = EduNavColors.LightText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Accessible Learning Assistant",
                    fontSize = 20.sp,
                    color = EduNavColors.VeryLightBlue,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Middle section with progress and status
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status text
                Text(
                    if (ttsStarted) "Listening to welcome message..." else "Initializing...",
                    fontSize = 16.sp,
                    color = EduNavColors.LightText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Progress bar
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = EduNavColors.AccentLightBlue,
                    trackColor = EduNavColors.VeryLightBlue
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Progress percentage
                Text(
                    "${(progress * 100).toInt()}%",
                    fontSize = 14.sp,
                    color = EduNavColors.AccentLightBlue,
                    textAlign = TextAlign.Center
                )
            }

            // Bottom section with buttons and version
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Skip button
                Button(
                    onClick = { onSkip() },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(48.dp)
                ) {
                    Text("Skip", fontSize = 18.sp)
                }

                // Tap to continue hint
                Text(
                    "or tap anywhere to continue",
                    fontSize = 13.sp,
                    color = EduNavColors.AccentLightBlue,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Version and device info
                Text(
                    "Version $appVersion",
                    fontSize = 11.sp,
                    color = EduNavColors.VeryLightBlue,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // Auto-navigate after TTS completes
    LaunchedEffect(isReadyToNavigate) {
        if (isReadyToNavigate) {
            onNavigateToHome()
        }
    }
}

@Composable
fun LoadingIndicator() {
    var dotCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            android.os.SystemClock.sleep(500)
            dotCount = (dotCount + 1) % 4
        }
    }

    val dots = ".".repeat(dotCount)
    Text(
        "Loading$dots",
        fontSize = 14.sp,
        color = EduNavColors.LightText
    )
}
