package com.example.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

interface TTSProvider {
    fun isReady(): Boolean
    fun speak(text: String, slow: Boolean = false, onDone: () -> Unit = {})
    fun stop()
    fun shutdown()
}

class AndroidTTSProvider(context: Context) : TTSProvider {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                isInitialized = true
            }
        }
    }

    override fun isReady(): Boolean = isInitialized

    override fun speak(text: String, slow: Boolean, onDone: () -> Unit) {
        if (!isInitialized || tts == null) return

        tts?.setSpeechRate(if (slow) 0.65f else 1.0f)
        val utteranceId = "utterance_${System.currentTimeMillis()}"

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {}
            override fun onDone(id: String?) {
                if (id == utteranceId) onDone()
            }
            override fun onError(id: String?) {}
        })

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    override fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
    }

    override fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (_: Exception) {}
    }
}
