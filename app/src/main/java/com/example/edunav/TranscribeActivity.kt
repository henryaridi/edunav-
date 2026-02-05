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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edunav.ui.theme.AppBackground
import com.example.edunav.ui.theme.EduNavTheme
import java.util.*

class TranscribeActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var isTranscribing by mutableStateOf(false)
    private var listeningForCommand by mutableStateOf(true)
    private val transcript = mutableStateListOf<String>()
    private var voiceSpeed = 1.0f

    private val handler = Handler(Looper.getMainLooper())

    private val speechRecognizerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val matches = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val spokenText = matches?.get(0)?.lowercase(Locale.ROOT) ?: ""
                if (isTranscribing) {
                    // Append to transcript and keep listening
                    if (spokenText.isNotBlank()) transcript.add(spokenText)
                    if (isTranscribing) handler.postDelayed({ startListening() }, 250)
                } else {
                    // Handle as a command
                    handleVoiceCommand(spokenText)
                }
            } else {
                speak("Sorry, I didn’t catch that. Please say it again.", "RETRY")
                // Retry listening for command mode
                handler.postDelayed({ startListening() }, 700)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        setContent {
            EduNavTheme {
                AppBackground {
                    TranscribeScreen(
                        transcript = transcript,
                        isTranscribing = isTranscribing,
                        onStart = { startTranscription() },
                        onStop = { stopTranscription() },
                        onListenCommand = { startCommandListening() }
                    )
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val prefs = getSharedPreferences("EduNavPrefs", Context.MODE_PRIVATE)
            voiceSpeed = prefs.getFloat("voiceSpeed", 1.0f)
            val langCode = prefs.getString("language", "en") ?: "en"
            tts.language = Locale(langCode)
            tts.setSpeechRate(voiceSpeed)

            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    // After intro finishes, start listening for command
                    if (utteranceId == "INTRO") {
                        handler.postDelayed({ startCommandListening() }, 500)
                    }
                }

                override fun onError(utteranceId: String?) {}
            })

            // Welcome and explain
            speakIntro()
        }
    }

    private fun speakIntro() {
        val intro = "Welcome to the Transcribe page. I can transcribe a lecture live. " +
                "Available voice commands are: Start transcription, Stop transcription, Open tutorials, Back, or Home. " +
                "Say a command now."
        speak(intro, "INTRO")
    }

    private fun speak(text: String, utteranceId: String?) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId ?: UUID.randomUUID().toString())
    }

    private fun startCommandListening() {
        listeningForCommand = true
        isTranscribing = false
        startListening("Say a command like: Start transcription")
    }

    private fun startTranscription() {
        transcript.clear()
        listeningForCommand = false
        isTranscribing = true
        speak("Starting transcription. I am listening.", "START_TX")
        handler.postDelayed({ startListening() }, 600)
    }

    private fun stopTranscription() {
        if (isTranscribing) {
            isTranscribing = false
            speak("Stopped transcription.", "STOP_TX")
        } else {
            speak("No active transcription to stop.", "STOP_TX")
        }
    }

    private fun handleVoiceCommand(command: String) {
        when {
            command.contains("start transcribe") || command.contains("start transcription") || command.contains("begin lecture") || command.contains("start lecture") -> {
                startTranscription()
            }
            command.contains("stop transcription") || command.contains("stop transcribe") || command.contains("stop lecture") -> {
                stopTranscription()
            }
            command.contains("open tutorial") || command.contains("open tutorials") || command.contains("tutorials") -> {
                speak("Opening tutorials.", "OPEN_TUT")
                // remain on page unless user explicitly asked to navigate away
                handler.postDelayed({
                    val intent = Intent(this, TutorialsActivity::class.java)
                    startActivity(intent)
                }, 600)
            }
            command.contains("back") || command.contains("home") -> {
                speak("Going back.", "GO_BACK")
                handler.postDelayed({ finish() }, 600)
            }
            command.isBlank() -> {
                speak("I didn't hear anything. Please try again.", "RETRY")
                handler.postDelayed({ startCommandListening() }, 800)
            }
            else -> {
                speak("Sorry, I didn't understand that command. Please try again.", "RETRY")
                handler.postDelayed({ startCommandListening() }, 800)
            }
        }
    }

    private fun startListening(prompt: String = "Say something") {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Speech recognition not supported", Toast.LENGTH_SHORT).show()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranscribeScreen(
    transcript: List<String>,
    isTranscribing: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onListenCommand: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
                .semantics { contentDescription = "Transcribe Page" },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Transcribe", style = MaterialTheme.typography.headlineLarge, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "This page transcribes spoken lecture into text. Use voice commands: Start transcription, Stop transcription, Open tutorials, Back or Home.",
                fontSize = 18.sp,
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { onStart() }) {
                    Icon(painter = painterResource(id = R.drawable.ic_microphone), contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start")
                }

                Button(onClick = { onStop() }) {
                    Icon(painter = painterResource(id = R.drawable.ic_stop), contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Stop")
                }

                Button(onClick = { onListenCommand() }) {
                    Icon(painter = painterResource(id = R.drawable.ic_voice), contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Listen")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Transcript:", fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (transcript.isEmpty()) {
                        Text("No text yet.", color = Color.Gray)
                    } else {
                        transcript.forEachIndexed { idx, line ->
                            Text("${idx + 1}. $line", maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(if (isTranscribing) "Status: Listening..." else "Status: Idle", color = Color.Cyan)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Say 'Start transcription' to begin or press Start.", color = Color.LightGray)
        }
    }
}
