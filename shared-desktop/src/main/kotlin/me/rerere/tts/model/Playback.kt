package me.rerere.tts.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class PlaybackStatus {
    Stopped,
    Playing,
    Paused,
    Buffering,
}

data class PlaybackState(
    val status: PlaybackStatus = PlaybackStatus.Stopped,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val currentChunkIndex: Int = 0,
    val totalChunks: Int = 0,
    val speed: Float = 1.0f,
)

data class TTSResponse(
    val id: String = "",
    val chunks: List<ByteArray> = emptyList(),
)
