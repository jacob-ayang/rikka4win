package me.rerere.tts.controller

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.rerere.tts.model.PlaybackState
import me.rerere.tts.model.PlaybackStatus
import me.rerere.tts.model.TTSResponse

class TtsController {
    private val _playbackState = MutableStateFlow(PlaybackState(status = PlaybackStatus.Stopped))
    val playbackState: StateFlow<PlaybackState> = _playbackState

    fun play(response: TTSResponse) {
        _playbackState.value = _playbackState.value.copy(status = PlaybackStatus.Playing)
    }

    fun pause() {
        _playbackState.value = _playbackState.value.copy(status = PlaybackStatus.Paused)
    }

    fun stop() {
        _playbackState.value = _playbackState.value.copy(status = PlaybackStatus.Stopped)
    }

    fun changeSpeed(speed: Float) {
        _playbackState.value = _playbackState.value.copy(speed = speed)
    }

    fun reset() {
        _playbackState.value = PlaybackState(status = PlaybackStatus.Stopped)
    }
}
