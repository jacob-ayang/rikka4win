package me.rerere.tts.provider

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import me.rerere.tts.model.AudioChunk
import me.rerere.tts.model.TTSRequest

class TTSManager(private val context: Context) {
    fun generateSpeech(
        providerSetting: TTSProviderSetting,
        request: TTSRequest
    ): Flow<AudioChunk> = emptyFlow()
}
