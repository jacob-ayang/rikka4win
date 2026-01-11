package me.rerere.tts.model

data class TTSRequest(
    val text: String,
    val speed: Float = 1.0f,
)

data class AudioChunk(val data: ByteArray)
