package me.rerere.rikkahub.ui.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.rerere.tts.model.PlaybackState

interface CustomTtsState {
    val isAvailable: StateFlow<Boolean>
    val isSpeaking: StateFlow<Boolean>
    val error: StateFlow<String?>
    val currentChunk: StateFlow<Int>
    val totalChunks: StateFlow<Int>
    val playbackState: StateFlow<PlaybackState>

    fun speak(text: String, flushCalled: Boolean = true)
    fun stop()
    fun pause()
    fun resume()
    fun skipNext()
    fun fastForward(ms: Long = 5_000)
    fun setSpeed(speed: Float)
    fun cleanup()
}

private class StubTtsState : CustomTtsState {
    override val isAvailable = MutableStateFlow(false)
    override val isSpeaking = MutableStateFlow(false)
    override val error = MutableStateFlow<String?>(null)
    override val currentChunk = MutableStateFlow(0)
    override val totalChunks = MutableStateFlow(0)
    override val playbackState = MutableStateFlow(PlaybackState())

    override fun speak(text: String, flushCalled: Boolean) = Unit
    override fun stop() = Unit
    override fun pause() = Unit
    override fun resume() = Unit
    override fun skipNext() = Unit
    override fun fastForward(ms: Long) = Unit
    override fun setSpeed(speed: Float) = Unit
    override fun cleanup() = Unit
}

@Composable
fun rememberCustomTtsState(): CustomTtsState {
    return remember { StubTtsState() }
}
