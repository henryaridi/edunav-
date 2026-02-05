package com.example.edunav

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edunav.ui.theme.AppBackground
import com.example.edunav.ui.theme.EduNavTheme
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var tts: TextToSpeech

    private val inactivityHandler = Handler(Looper.getMainLooper())
    private val inactivityRunnable = Runnable {
        tts.speak(
            "Are you still there? You can use buttons or your voice to navigate the app.",
            TextToSpeech.QUEUE_FLUSH,
            null,
            "IDLE_REMINDER"
        )
    }

    private fun resetInactivityTimer() {
        inactivityHandler.removeCallbacks(inactivityRunnable)
        inactivityHandler.postDelayed(inactivityRunnable, 60_000) // 1 minute idle
    }

    private val speechRecognizerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val matches = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.get(0)?.lowercase(Locale.ROOT) ?: ""
            handleVoiceCommand(spokenText)
        }
    }

    private val featureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            intent.putExtra("fromFeature", true)
        }
    }

    private fun handleVoiceCommand(command: String) {
        when {
            command.contains("class") -> {
                tts.speak("Navigating to Class", TextToSpeech.QUEUE_FLUSH, null, null)
                inactivityHandler.removeCallbacks(inactivityRunnable)
                featureLauncher.launch(Intent(this, ClassActivity::class.java))
            }
            command.contains("transcribe") -> {
                tts.speak("Starting lecture transcription", TextToSpeech.QUEUE_FLUSH, null, null)
                inactivityHandler.removeCallbacks(inactivityRunnable)
                featureLauncher.launch(Intent(this, TranscribeActivity::class.java))
            }
            command.contains("braille") -> {
                tts.speak("Opening Braille Tutorials", TextToSpeech.QUEUE_FLUSH, null, null)
                inactivityHandler.removeCallbacks(inactivityRunnable)
                featureLauncher.launch(Intent(this, TutorialsActivity::class.java))
            }
            else -> {
                tts.speak("Sorry, I didn’t understand that. Please try again.", TextToSpeech.QUEUE_FLUSH, null, null)
                Handler(Looper.getMainLooper()).postDelayed({
                    startListening()
                }, 1000)
            }
        }
        resetInactivityTimer()
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a command like 'open class'")
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Speech recognition not supported", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val prefs = getSharedPreferences("EduNavPrefs", Context.MODE_PRIVATE)
                val speed = prefs.getFloat("voiceSpeed", 1.0f)
                val langCode = prefs.getString("language", "en")
                tts.language = Locale(langCode ?: "en")
                tts.setSpeechRate(speed)

                // Utterance listener stays intact
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            startListening()
                        }, 1000)
                    }
                    override fun onError(utteranceId: String?) {}
                })

                // Speak welcome message on launch
                speakOnLaunch()

                // Start inactivity timer
                resetInactivityTimer()
            }
        }

        setContent { EduNavApp(this) }
    }

    private fun speakOnLaunch() {
        val prefs = getSharedPreferences("EduNavPrefs", Context.MODE_PRIVATE)
        val hasWelcomed = prefs.getBoolean("hasWelcomed", false)

        val welcomeText = "Welcome to EduNav home screen. " +
                "Use the first button to navigate to Class, second button to Transcribe a lecture, " +
                "and third button to access Braille tutorials. You can also use your voice to navigate."

        if (!hasWelcomed) {
            tts.speak(welcomeText, TextToSpeech.QUEUE_FLUSH, null, "WELCOME_MSG")
            prefs.edit().putBoolean("hasWelcomed", true).apply()
        } else {
            tts.speak(welcomeText, TextToSpeech.QUEUE_ADD, null, "WELCOME_MSG")
        }
    }

    private fun speakWelcomeBack() {
        val welcomeBackText = "Welcome back to the EduNav Home screen."
        tts.speak(welcomeBackText, TextToSpeech.QUEUE_ADD, null, null)
    }

    override fun onResume() {
        super.onResume()
        if (intent.getBooleanExtra("fromFeature", false)) {
            speakWelcomeBack()
            intent.removeExtra("fromFeature")
        }
        resetInactivityTimer()
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.speak("Thank you for using EduNav. Have a great day.", TextToSpeech.QUEUE_FLUSH, null, null)
            Thread.sleep(1000)
            tts.stop()
            tts.shutdown()
        }
        inactivityHandler.removeCallbacks(inactivityRunnable)
        super.onDestroy()
    }

    @Composable
    fun EduNavApp(activity: ComponentActivity) {
        EduNavTheme {
            AppBackground {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .semantics { contentDescription = "EduNav Home Screen" },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Navigate to Class
                    Button(
                        onClick = {
                            tts.speak("Navigating to Class", TextToSpeech.QUEUE_FLUSH, null, null)
                            inactivityHandler.removeCallbacks(inactivityRunnable)
                            val intent = Intent(activity, ClassActivity::class.java)
                            intent.putExtra(
                                "message",
                                "Hello, Welcome to the Navigation section. This will help you navigate to Class."
                            )
                            featureLauncher.launch(intent)
                        },
                        modifier = Modifier.semantics { contentDescription = "Navigate to class" }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(painter = painterResource(id = R.drawable.ic_class), contentDescription = null, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Navigate to Class", color = Color.White, fontSize = 25.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Transcribe Lecture
                    Button(
                        onClick = {
                            tts.speak("Starting lecture transcription", TextToSpeech.QUEUE_FLUSH, null, null)
                            inactivityHandler.removeCallbacks(inactivityRunnable)
                            val intent = Intent(activity, TranscribeActivity::class.java)
                            intent.putExtra("message", "Hello, you are transcribing a lecture.")
                            featureLauncher.launch(intent)
                        },
                        modifier = Modifier.semantics { contentDescription = "Start lecture transcription" }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(painter = painterResource(id = R.drawable.ic_transcribe), contentDescription = null, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Transcribe Lecture", color = Color.White, fontSize = 25.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Braille Tutorials
                    Button(
                        onClick = {
                            tts.speak("Opening Braille Tutorials", TextToSpeech.QUEUE_FLUSH, null, null)
                            inactivityHandler.removeCallbacks(inactivityRunnable)
                            val intent = Intent(activity, TutorialsActivity::class.java)
                            intent.putExtra("message", "Hello, Welcome to Braille Tutorials.")
                            featureLauncher.launch(intent)
                        },
                        modifier = Modifier.semantics { contentDescription = "Open Braille tutorials" }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(painter = painterResource(id = R.drawable.ic_braille), contentDescription = null, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Open Braille Tutorials", color = Color.White, fontSize = 25.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Settings
                    Button(
                        onClick = {
                            val intent = Intent(activity, SettingsActivity::class.java)
                            inactivityHandler.removeCallbacks(inactivityRunnable)
                            activity.startActivity(intent)
                        },
                        modifier = Modifier.semantics { contentDescription = "Open Settings" }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(painter = painterResource(id = R.drawable.ic_settings), contentDescription = null, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Settings", color = Color.White, fontSize = 25.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}
