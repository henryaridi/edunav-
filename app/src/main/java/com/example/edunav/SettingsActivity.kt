package com.example.edunav

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.edunav.ui.theme.AppBackground
import com.example.edunav.ui.theme.EduNavTheme
import java.util.*

class SettingsActivity : ComponentActivity(), TextToSpeech.OnInitListener {

	private lateinit var tts: TextToSpeech
	private var ttsReady by mutableStateOf(false)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		tts = TextToSpeech(this, this)
		val prefs = getSharedPreferences("EduNavPrefs", Context.MODE_PRIVATE)

		setContent {
			EduNavTheme {
				AppBackground {
					SettingsScreen(
						prefs = prefs,
						speak = { text, lang, speed, pitch ->
							speakText(text, lang, speed, pitch)
						},
						context = this
					)
				}
			}
		}
	}

	override fun onInit(status: Int) {
		ttsReady = status == TextToSpeech.SUCCESS
	}

	private fun speakText(
		text: String,
		lang: String,
		speed: Float,
		pitch: Float
	) {
		if (!ttsReady) return

		tts.language = Locale(lang)
		tts.setSpeechRate(speed)
		tts.setPitch(pitch)
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, text.hashCode().toString())
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
fun SettingsScreen(
	prefs: android.content.SharedPreferences,
	speak: (String, String, Float, Float) -> Unit,
	context: Context
) {
	val languages = listOf("en" to "English", "sw" to "Swahili")

	var selectedLang by remember { mutableStateOf(prefs.getString("language", "en") ?: "en") }
	var speed by remember { mutableStateOf(prefs.getFloat("voiceSpeed", 1.0f)) }
	var pitch by remember { mutableStateOf(prefs.getFloat("voicePitch", 1.0f)) }
	var speakConfirmations by remember { mutableStateOf(prefs.getBoolean("speakConfirmations", true)) }
	var autoTranscribe by remember { mutableStateOf(prefs.getBoolean("autoTranscribe", false)) }

	Surface(modifier = Modifier.fillMaxSize()) {
		Column(
			modifier = Modifier.padding(16.dp),
			horizontalAlignment = Alignment.Start
		) {

			Text("Settings", style = MaterialTheme.typography.headlineLarge)
			Spacer(Modifier.height(12.dp))

			Text("Language", style = MaterialTheme.typography.titleMedium)
			Spacer(Modifier.height(6.dp))

			languages.forEach { (code, label) ->
				Button(
					onClick = {
						selectedLang = code
						prefs.edit().putString("language", code).apply()

						if (speakConfirmations) {
							speak("Language set to $label", code, speed, pitch)
						}
					},
					modifier = Modifier.fillMaxWidth()
				) {
					Text(if (selectedLang == code) "✓ $label" else label)
				}
				Spacer(Modifier.height(6.dp))
			}

			Divider(Modifier.padding(vertical = 12.dp))

			Text("Voice Speed")
			Slider(value = speed, onValueChange = { speed = it }, valueRange = 0.5f..2f)
			Text(String.format(Locale.getDefault(), "%.2fx", speed))

			Spacer(Modifier.height(12.dp))

			Text("Voice Pitch")
			Slider(value = pitch, onValueChange = { pitch = it }, valueRange = 0.5f..2f)
			Text(String.format(Locale.getDefault(), "%.2fx", pitch))

			Divider(Modifier.padding(vertical = 12.dp))

			Row(verticalAlignment = Alignment.CenterVertically) {
				Text("Speak confirmations")
				Spacer(Modifier.weight(1f))
				Switch(checked = speakConfirmations, onCheckedChange = { speakConfirmations = it })
			}

			Row(verticalAlignment = Alignment.CenterVertically) {
				Text("Auto-start transcription")
				Spacer(Modifier.weight(1f))
				Switch(checked = autoTranscribe, onCheckedChange = { autoTranscribe = it })
			}

			Spacer(Modifier.height(16.dp))

			Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
				Button(onClick = {
					prefs.edit()
						.putString("language", selectedLang)
						.putFloat("voiceSpeed", speed)
						.putFloat("voicePitch", pitch)
						.putBoolean("speakConfirmations", speakConfirmations)
						.putBoolean("autoTranscribe", autoTranscribe)
						.apply()

					if (speakConfirmations) {
						speak("Settings saved.", selectedLang, speed, pitch)
					} else {
						Toast.makeText(context, "Settings saved", Toast.LENGTH_SHORT).show()
					}
				}) {
					Text("Save")
				}

				Button(onClick = {
					speak(
						"This is a preview of the current voice settings.",
						selectedLang,
						speed,
						pitch
					)
				}) {
					Text("Preview")
				}
			}
		}
	}
}
