package com.example.edunav

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class SplashActivity : AppCompatActivity() {

    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Initialize TTS
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.ENGLISH
                speakWelcomeAndLaunch()
            }
        }
    }

    private fun speakWelcomeAndLaunch() {
        tts.speak(
            "Welcome to EduNav.",
            TextToSpeech.QUEUE_FLUSH,
            null,
"welcomeMessage"
        )

        Handler(Looper.getMainLooper()).postDelayed({
            redirectToMain()
        }, 2000)
    }

    private fun redirectToMain() {
        val intent = Intent(this, MainActivity::class.java)
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
