package com.example.edunav

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.TextToSpeech
import java.util.Locale

object VoiceCommandHelper {

    fun handleCommand(
        context: Context,
        spokenText: String,
        tts: TextToSpeech,
        retry: () -> Unit
    ) {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}

            override fun onDone(utteranceId: String?) {
                Handler(Looper.getMainLooper()).post {
                    when (utteranceId) {
                        "UNRECOGNIZED" -> {
                            Handler(Looper.getMainLooper()).postDelayed({
                                tts.speak("Listening", TextToSpeech.QUEUE_FLUSH, null, "LISTENING_PROMPT")
                            }, 1000)
                        }

                        "LISTENING_PROMPT" -> {
                            retry()
                        }
                    }
                }
            }

            override fun onError(utteranceId: String?) {}
        })

        val lowerText = spokenText.lowercase(Locale.ROOT)

        when {
            lowerText.contains("home") -> {
                tts.speak("Navigating  to home screen", TextToSpeech.QUEUE_FLUSH, null, null)
                context.startActivity(Intent(context, MainActivity::class.java))
                if (context is Activity) context.finish()
            }

            lowerText.contains("class") -> {
                tts.speak("Opening class section", TextToSpeech.QUEUE_FLUSH, null, null)
                context.startActivity(Intent(context, ClassActivity::class.java))
                if (context is Activity) context.finish()
            }

            lowerText.contains("braille") || lowerText.contains("tutorial") -> {
                tts.speak("Opening braille tutorial", TextToSpeech.QUEUE_FLUSH, null, null)
                context.startActivity(Intent(context, TutorialsActivity::class.java))
                if (context is Activity) context.finish()
            }

            lowerText.contains("lecture") || lowerText.contains("transcribe") -> {
                tts.speak("Opening transcription page", TextToSpeech.QUEUE_FLUSH, null, null)
                context.startActivity(Intent(context, TranscribeActivity::class.java))
                if (context is Activity) context.finish()
            }

            lowerText.contains("back") -> {
                tts.speak("Going back", TextToSpeech.QUEUE_FLUSH, null, null)
                if (context is Activity) context.finish()
            }

            else -> {
                tts.speak(
                    "Sorry, I didn't understand the command. Please say it again.",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "UNRECOGNIZED"
                )
            }
        }
    }
}
