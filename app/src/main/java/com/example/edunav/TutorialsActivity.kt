package com.example.edunav

import com.example.edunav.ui.theme.EduNavTheme
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edunav.ui.theme.AppBackground
import java.util.*

class TutorialsActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var locale: Locale
    private val handler = Handler(Looper.getMainLooper())

    private val speechRecognizerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val matches = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.get(0)?.lowercase(Locale.ROOT) ?: ""

            handleVoiceCommand(spokenText)

        } else {
            speak("I didn't catch that. Please try again.")
            handler.postDelayed({ startListening() }, 1200)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("EduNavPrefs", Context.MODE_PRIVATE)
        val voiceSpeed = prefs.getFloat("voiceSpeed", 1.0f)
        val languageCode = prefs.getString("language", "en") ?: "en"

        locale = when (languageCode) {
            "fr" -> Locale.FRENCH
            "sw" -> Locale("sw")
            "es" -> Locale("es")
            else -> Locale.ENGLISH
        }

        tts = TextToSpeech(this, this)
        tts.setSpeechRate(voiceSpeed)

        setContent {
            TutorialsScreen(this, tts, ::startListening)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = locale

            speak(
                "Welcome to the Tutorials section. " +
                        "Here, you can learn topics such as Alphabet and Numbers. " +
                        "To begin, say the name of a topic. " +
                        "To go back home, say Back."
            )

            handler.postDelayed({ startListening() }, 3000)
        }
    }
    private fun handleVoiceCommand(command: String) {
        when {
            command.contains("back") -> {
                speak("Returning home.")
                finish()
            }

            command.contains("tutorial") || command.contains("braille") -> {
                speak("Opening Braille Tutorials.")
                startActivity(Intent(this, TutorialsActivity::class.java))
            }

            command.contains("transcribe") || command.contains("record") -> {
                speak("Starting transcription.")
                // Your recording function or UI here
            }

            else -> {
                speak("Sorry, I didn't understand. Please say Back, Braille, or Start Recording.")
                handler.postDelayed({ startListening() }, 1000)
            }
        }
    }

    private fun speak(message: String) {
        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Listening...")
        }
        speechRecognizerLauncher.launch(intent)
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
fun TutorialsScreen(activity: Activity, tts: TextToSpeech, onVoiceCommand: () -> Unit) {
    EduNavTheme {
        AppBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text("Learn Braille topics such as Alphabet and Numbers.", fontSize = 24.sp, color = Color.White)
                Spacer(modifier = Modifier.height(20.dp))

                Button(onClick = {
                    tts.speak("Going back", TextToSpeech.QUEUE_FLUSH, null, null)
                    activity.setResult(Activity.RESULT_OK)
                    activity.finish()
                }) {
                    Text("Back", color = Color.White, fontSize = 30.sp)
                }
            }
        }
    }
}
