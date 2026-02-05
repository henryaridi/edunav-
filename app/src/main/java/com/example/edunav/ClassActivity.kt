package com.example.edunav

import com.example.edunav.ui.theme.EduNavTheme
import com.example.edunav.ui.theme.AppBackground
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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
import java.util.*

class ClassActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var locale: Locale
    private var voiceSpeed = 1.0f

    private val speechRecognizerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val matches = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val spokenText = matches?.get(0)?.lowercase(Locale.ROOT) ?: ""
                handleVoiceCommand(spokenText)
            } else {
                speak("Sorry, I didn’t catch that. Please say it again.", "RETRY")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("EduNavPrefs", Context.MODE_PRIVATE)
        voiceSpeed = prefs.getFloat("voiceSpeed", 1.0f)

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
            EduNavTheme {
                ClassScreen(this@ClassActivity, tts)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = locale
            speak(
                "Welcome to the Navigation section. This page helps you navigate to class. " +
                        "Please be aware of your surroundings while moving. " +
                        "You can say Back, Open Tutorials, or Transcribe Lecture.",
                "WELCOME"
            )
        }
    }

    private fun speak(message: String, id: String) {
        if (!::tts.isInitialized) return

        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, id)

        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {}
            override fun onError(id: String?) {}

            override fun onDone(utteranceId: String?) {
                if (utteranceId == "WELCOME" || utteranceId == "RETRY") {
                    startListening()
                }
            }
        })
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale)
        }

        speechRecognizerLauncher.launch(intent)
    }

    private fun handleVoiceCommand(command: String) {
        when {
            command.contains("back") || command.contains("home") -> {
                speak("Returning home.", "NONE")
                finish()
            }

            command.contains("tutorial") || command.contains("braille") -> {
                speak("Opening Braille Tutorials.", "NONE")
                startActivity(Intent(this, TutorialsActivity::class.java))
            }

            command.contains("transcribe") || command.contains("lecture") -> {
                speak("Opening transcription page.", "NONE")
                startActivity(Intent(this, TranscribeActivity::class.java))
            }

            else -> {
                speak(
                    "Sorry, I didn’t understand that. Please say it again.",
                    "RETRY"
                )
            }
        }
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
fun ClassScreen(activity: Activity, tts: TextToSpeech) {
    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("Welcome to Navigation Section", fontSize = 30.sp, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))

            // ⭐ Safety Notice
            Text(
                "Please be aware of your surroundings while moving.",
                fontSize = 20.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(30.dp))

            // ⭐ Start Navigation Button for partially blind users
            Button(
                onClick = {
                    tts.speak("Starting navigation.", TextToSpeech.QUEUE_FLUSH, null, "NONE")
                    // TODO: Add your navigation logic here
                },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Start Navigation", color = Color.White, fontSize = 26.sp)
            }

            Spacer(modifier = Modifier.height(25.dp))

            Button(onClick = {
                tts.speak("Back", TextToSpeech.QUEUE_FLUSH, null, "NONE")
                activity.finish()
            }) {
                Text("Back", color = Color.White, fontSize = 26.sp)
            }
        }
    }
}
