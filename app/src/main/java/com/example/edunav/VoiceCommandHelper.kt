package com.example.edunav

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import java.util.*

/**
 * VoiceCommandHelper provides centralized voice command definitions, instruction strings,
 * and TTS utilities for the EduNav app.
 */
object VoiceCommandHelper {

    // ===== VOICE COMMAND KEYWORDS =====
    object Commands {
        // Navigation commands
        const val START_TRANSCRIPTION = "start transcribe|start transcription|begin lecture|start lecture"
        const val STOP_TRANSCRIPTION = "stop transcribe|stop transcription|stop lecture"
        const val OPEN_TUTORIALS = "open tutorial|open tutorials|tutorials"
        const val OPEN_CLASS = "open class|navigate class|class"
        const val OPEN_BRAILLE = "open braille|braille"
        const val BACK = "back"
        const val HOME = "home|go home"
        const val SETTINGS = "settings|open settings"
        const val HELP = "help|instructions"
        const val REPEAT = "repeat|say again"
    }

    // ===== INSTRUCTION & WELCOME MESSAGES =====
    object Instructions {
        const val HOME_WELCOME =
            "Welcome to EduNav. I am an accessible learning assistant for blind and visually impaired students. " +
                    "Use buttons or voice commands to navigate. First button navigates to Class, second button transcribes lectures, " +
                    "third button opens Braille tutorials, and fourth button opens Settings. What would you like to do?"

        const val HOME_QUICK_HELP =
            "Available commands on the home screen: Open Class, Transcribe, Tutorials, Settings, Home, or Help. Say a command now."

        const val TRANSCRIBE_WELCOME =
            "Welcome to the Transcribe page. I can transcribe a lecture live. " +
                    "Say Start transcription or press Start to begin. " +
                    "While transcribing, speak clearly and pause between sentences. " +
                    "Say Stop transcription to end. Available commands: Start transcription, Stop transcription, Open tutorials, Back, Home."

        const val TRANSCRIBE_INSTRUCTIONS =
            "Transcription mode active. I am listening. Speak your lecture content. " +
                    "I will capture every sentence you say. You can say Stop transcription to end."

        const val CLASS_WELCOME =
            "Welcome to the Class Navigation page. Here you can navigate your class schedule and materials. " +
                    "Available commands: Previous class, Next class, View details, Back, Home."

        const val BRAILLE_WELCOME =
            "Welcome to Braille Tutorials. You can learn Braille basics here. " +
                    "Available commands: Next lesson, Previous lesson, Repeat, Back, Home."

        const val SETTINGS_WELCOME =
            "Welcome to Settings. Here you can customize language, voice speed, pitch, and other preferences. " +
                    "You can select language, adjust voice speed and pitch, toggle voice preferences, and save or reset settings."

        const val SETTINGS_INSTRUCTIONS =
            "You can change the following: Language (English or Swahili), Voice speed (0.5 to 2.0 times), " +
                    "Voice pitch (0.5 to 2.0 times), Female voice toggle, Speak confirmations, and Auto-start transcription. " +
                    "Press Save to save your changes or Reset to go back to defaults."

        const val COMMAND_NOT_RECOGNIZED =
            "Sorry, I did not recognize that command. Please try again. You can say Help for available commands."

        const val COMMAND_RETRY =
            "I did not hear you clearly. Please speak again."

        const val NOTHING_HEARD =
            "I did not hear anything. Please try again."

        const val SETTINGS_SAVED =
            "Your settings have been saved successfully."

        const val SETTINGS_RESET =
            "Settings have been reset to defaults."

        const val GOING_BACK =
            "Going back to the previous page."

        const val RETURNING_HOME =
            "Returning to the home screen."

        const val TRANSCRIPTION_STARTED =
            "Transcription started. I am listening to your lecture. Speak clearly."

        const val TRANSCRIPTION_STOPPED =
            "Transcription stopped. You can review or save your transcript."

        const val PREVIEW_MESSAGE =
            "This is a preview of your current voice settings."

        const val LANGUAGE_CHANGED =
            "Language has been changed."

        const val VOICE_SPEED_ADJUSTED =
            "Voice speed has been adjusted."

        const val VOICE_PITCH_ADJUSTED =
            "Voice pitch has been adjusted."

        const val TOGGLE_ENABLED =
            "This option has been enabled."

        const val TOGGLE_DISABLED =
            "This option has been disabled."

        const val TUTORIAL_LESSON_INTRO =
            "This is lesson one of Braille tutorials. Learn the basic Braille dot patterns."

        const val CLASS_NAVIGATION_INTRO =
            "Class navigation allows you to explore your courses and materials. You have several classes available."

        const val TRANSCRIPTION_PAUSE_REMINDER =
            "Remember to pause briefly between sentences for clearer transcription."

        const val ACCESSIBILITY_REMINDER =
            "EduNav is designed to be fully accessible. All features are available via voice commands or touch buttons."

        const val UNSAVED_CHANGES =
            "You have unsaved changes. Do you want to save them before leaving?"

        const val CONFIRM_RESET =
            "Are you sure you want to reset all settings to defaults? Say yes to confirm or no to cancel."
    }

    // ===== VOICE COMMAND HINTS =====
    object CommandHints {
        const val MAIN_COMMANDS = "You can say: Open Class, Transcribe, Tutorials, Settings, or Help."
        const val TRANSCRIBE_COMMANDS = "Say: Start transcription, Stop transcription, Open tutorials, Back, or Home."
        const val CLASS_COMMANDS = "Say: Next class, Previous class, View details, Back, or Home."
        const val BRAILLE_COMMANDS = "Say: Next lesson, Previous lesson, Repeat, Back, or Home."
        const val SETTINGS_COMMANDS = "Say: Save, Preview, Reset, or Back."
    }

    // ===== TTS UTILITY METHODS =====

    /**
     * Speaks a message using TextToSpeech.
     *
     * @param tts TextToSpeech instance
     * @param text Text to speak
     * @param utteranceId Optional unique identifier for the utterance
     * @param queue TextToSpeech queue mode (QUEUE_FLUSH or QUEUE_ADD)
     */
    fun speak(
        tts: TextToSpeech,
        text: String,
        utteranceId: String? = null,
        queue: Int = TextToSpeech.QUEUE_FLUSH
    ) {
        val id = utteranceId ?: UUID.randomUUID().toString()
        tts.speak(text, queue, null, id)
    }

    /**
     * Speaks a message and then performs an action after a delay.
     *
     * @param tts TextToSpeech instance
     * @param text Text to speak
     * @param delayMillis Delay in milliseconds before executing the action
     * @param action Lambda function to execute after TTS finishes
     * @param utteranceId Optional utterance ID
     */
    fun speakAndThen(
        tts: TextToSpeech,
        text: String,
        delayMillis: Long = 1000,
        utteranceId: String? = null,
        action: () -> Unit
    ) {
        speak(tts, text, utteranceId ?: "SPEAK_THEN")
        Handler(Looper.getMainLooper()).postDelayed({
            action.invoke()
        }, delayMillis)
    }

    /**
     * Configures TTS with language, speed, and pitch from SharedPreferences.
     *
     * @param context Android context
     * @param tts TextToSpeech instance
     * @return Configured Locale
     */
    fun setupTTSFromPreferences(context: Context, tts: TextToSpeech): Locale {
        val prefs = context.getSharedPreferences("EduNavPrefs", Context.MODE_PRIVATE)
        val langCode = prefs.getString("language", "en") ?: "en"
        val speed = prefs.getFloat("voiceSpeed", 1.0f)
        val pitch = prefs.getFloat("voicePitch", 1.0f)

        val locale = Locale(langCode)
        tts.language = locale
        tts.setSpeechRate(speed)
        tts.setPitch(pitch)

        return locale
    }

    // ===== COMMAND MATCHING & PARSING =====

    /**
     * Checks if the user's spoken input matches a command pattern.
     *
     * @param input User's spoken input
     * @param commandPattern Pipe-separated command keywords
     * @return True if input matches the pattern
     */
    fun matchesCommand(input: String, commandPattern: String): Boolean {
        val patterns = commandPattern.split("|")
        return patterns.any { input.lowercase(Locale.ROOT).contains(it.lowercase()) }
    }

    /**
     * Parses the user's voice input and returns the matched command type.
     *
     * @param input User's spoken input
     * @return Command type as a string, or null if no match
     */
    fun parseCommand(input: String): String? {
        val lowerInput = input.lowercase(Locale.ROOT)

        return when {
            matchesCommand(lowerInput, Commands.START_TRANSCRIPTION) -> "START_TRANSCRIPTION"
            matchesCommand(lowerInput, Commands.STOP_TRANSCRIPTION) -> "STOP_TRANSCRIPTION"
            matchesCommand(lowerInput, Commands.OPEN_TUTORIALS) -> "OPEN_TUTORIALS"
            matchesCommand(lowerInput, Commands.OPEN_CLASS) -> "OPEN_CLASS"
            matchesCommand(lowerInput, Commands.OPEN_BRAILLE) -> "OPEN_BRAILLE"
            matchesCommand(lowerInput, Commands.SETTINGS) -> "OPEN_SETTINGS"
            matchesCommand(lowerInput, Commands.BACK) -> "BACK"
            matchesCommand(lowerInput, Commands.HOME) -> "HOME"
            matchesCommand(lowerInput, Commands.HELP) -> "HELP"
            matchesCommand(lowerInput, Commands.REPEAT) -> "REPEAT"
            else -> null
        }
    }

    /**
     * Validates if the input is a valid/recognized command.
     *
     * @param input User's spoken input
     * @return True if command is recognized
     */
    fun isValidCommand(input: String): Boolean {
        return parseCommand(input) != null
    }

    /**
     * Gets appropriate feedback message based on the context.
     *
     * @param context Context string (e.g., "HOME", "TRANSCRIBE", "SETTINGS")
     * @return Appropriate instruction message
     */
    fun getContextualInstruction(context: String): String {
        return when (context.uppercase(Locale.ROOT)) {
            "HOME" -> Instructions.HOME_QUICK_HELP
            "TRANSCRIBE" -> Instructions.TRANSCRIBE_INSTRUCTIONS
            "CLASS" -> Instructions.CLASS_NAVIGATION_INTRO
            "BRAILLE" -> Instructions.TUTORIAL_LESSON_INTRO
            "SETTINGS" -> Instructions.SETTINGS_INSTRUCTIONS
            else -> Instructions.HOME_QUICK_HELP
        }
    }

    /**
     * Provides a list of all available commands for help/accessibility.
     *
     * @return Formatted string with all available commands
     */
    fun getAllCommands(): String {
        return """
            NAVIGATION COMMANDS:
            1. Open Class - Navigate to your class materials
            2. Transcribe - Start transcribing a lecture
            3. Tutorials - Access Braille learning tutorials
            4. Settings - Adjust voice and language preferences
            5. Back - Return to previous page
            6. Home - Return to home screen
            7. Help - Hear this help message
            
            TRANSCRIPTION COMMANDS:
            - Start transcription - Begin lecture recording
            - Stop transcription - End lecture recording
            
            For more help, visit the Settings page to adjust voice preferences.
        """.trimIndent()
    }

    /**
     * Creates a formatted spoken instruction for the current page.
     *
     * @param pageName Name of the current page
     * @param isFirstVisit Whether this is the user's first visit
     * @return Welcome and instruction message
     */
    fun getPageWelcome(pageName: String, isFirstVisit: Boolean = true): String {
        return when (pageName.uppercase(Locale.ROOT)) {
            "HOME" -> if (isFirstVisit) Instructions.HOME_WELCOME else "Welcome back to EduNav."
            "TRANSCRIBE" -> Instructions.TRANSCRIBE_WELCOME
            "CLASS" -> Instructions.CLASS_WELCOME
            "BRAILLE" -> Instructions.BRAILLE_WELCOME
            "SETTINGS" -> Instructions.SETTINGS_WELCOME
            else -> "Welcome to EduNav."
        }
    }

    /**
     * Handles unrecognized commands with retry logic.
     *
     * @param tts TextToSpeech instance
     * @param onRetry Callback to retry listening
     * @param context Optional context for instruction
     */
    fun handleUnrecognizedCommand(
        tts: TextToSpeech,
        onRetry: () -> Unit,
        context: String? = null
    ) {
        speak(tts, Instructions.COMMAND_NOT_RECOGNIZED)
        Handler(Looper.getMainLooper()).postDelayed({
            if (context != null) {
                speak(tts, getContextualInstruction(context))
            }
            onRetry.invoke()
        }, 2000)
    }

    /**
     * Formats confirmation speech based on user action.
     *
     * @param action The action performed
     * @return Confirmation message
     */
    fun getConfirmationMessage(action: String): String {
        return when (action.uppercase(Locale.ROOT)) {
            "SAVE" -> Instructions.SETTINGS_SAVED
            "RESET" -> Instructions.SETTINGS_RESET
            "BACK" -> Instructions.GOING_BACK
            "HOME" -> Instructions.RETURNING_HOME
            "START_TRANSCRIBE" -> Instructions.TRANSCRIPTION_STARTED
            "STOP_TRANSCRIBE" -> Instructions.TRANSCRIPTION_STOPPED
            "LANGUAGE" -> Instructions.LANGUAGE_CHANGED
            "SPEED" -> Instructions.VOICE_SPEED_ADJUSTED
            "PITCH" -> Instructions.VOICE_PITCH_ADJUSTED
            else -> "Action completed."
        }
    }
}
