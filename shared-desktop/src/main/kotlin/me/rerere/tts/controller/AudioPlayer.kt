package me.rerere.tts.controller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import me.rerere.tts.model.AudioFormat
import me.rerere.tts.model.PlaybackState
import me.rerere.tts.model.PlaybackStatus
import me.rerere.tts.model.TTSResponse
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.FloatControl
import javax.sound.sampled.LineEvent
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AudioPlayer(
    @Suppress("UNUSED_PARAMETER") context: android.content.Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var clip: Clip? = null
    private var positionJob: Job? = null
    private var speed: Float = 1.0f
    private var pausedByUser = false

    fun pause() {
        pausedByUser = true
        clip?.stop()
        _playbackState.update { it.copy(status = PlaybackStatus.Paused) }
        stopPositionUpdates()
    }

    fun resume() {
        pausedByUser = false
        clip?.start()
        _playbackState.update { it.copy(status = PlaybackStatus.Playing) }
        startPositionUpdates()
    }

    fun stop() {
        pausedByUser = false
        clip?.stop()
        clip?.microsecondPosition = 0
        _playbackState.update { it.copy(status = PlaybackStatus.Idle, positionMs = 0L) }
        stopPositionUpdates()
    }

    fun clear() {
        stop()
        clip?.close()
        clip = null
    }

    fun release() {
        clear()
    }

    fun seekBy(ms: Long) {
        val c = clip ?: return
        val next = (c.microsecondPosition + ms * 1000).coerceIn(0, c.microsecondLength)
        c.microsecondPosition = next
        _playbackState.update { it.copy(positionMs = next / 1000) }
    }

    fun setSpeed(speed: Float) {
        this.speed = speed
        clip?.let { applySpeed(it, speed) }
        _playbackState.update { it.copy(speed = speed) }
    }

    suspend fun play(response: TTSResponse) = suspendCancellableCoroutine<Unit> { cont ->
        runCatching {
            clear()

            val bytes = when (response.format) {
                AudioFormat.PCM -> pcmToWav(response.audioData, response.sampleRate ?: 24000)
                else -> response.audioData
            }

            val stream = AudioSystem.getAudioInputStream(ByteArrayInputStream(bytes))
            val newClip = AudioSystem.getClip()
            clip = newClip

            var completed = false
            newClip.addLineListener { event ->
                when (event.type) {
                    LineEvent.Type.START -> {
                        _playbackState.update {
                            it.copy(
                                status = PlaybackStatus.Playing,
                                durationMs = newClip.microsecondLength / 1000,
                                positionMs = newClip.microsecondPosition / 1000,
                                speed = speed
                            )
                        }
                        startPositionUpdates()
                    }

                    LineEvent.Type.STOP -> {
                        stopPositionUpdates()
                        val atEnd = newClip.microsecondPosition >= (newClip.microsecondLength - 1_000)
                        if (atEnd && !pausedByUser) {
                            _playbackState.update {
                                it.copy(
                                    status = PlaybackStatus.Ended,
                                    positionMs = newClip.microsecondLength / 1000,
                                    durationMs = newClip.microsecondLength / 1000
                                )
                            }
                            if (!completed && cont.isActive) {
                                completed = true
                                cont.resume(Unit)
                            }
                        } else if (pausedByUser) {
                            _playbackState.update { it.copy(status = PlaybackStatus.Paused) }
                        } else {
                            _playbackState.update { it.copy(status = PlaybackStatus.Idle) }
                        }
                    }

                    else -> Unit
                }
            }

            newClip.open(stream)
            applySpeed(newClip, speed)
            _playbackState.update {
                it.copy(
                    status = PlaybackStatus.Buffering,
                    durationMs = newClip.microsecondLength / 1000,
                    positionMs = 0L
                )
            }
            newClip.start()

            cont.invokeOnCancellation {
                runCatching {
                    completed = true
                    clear()
                }
            }
        }.onFailure { err ->
            _playbackState.update {
                it.copy(status = PlaybackStatus.Error, errorMessage = err.message)
            }
            if (cont.isActive) cont.resumeWithException(err)
        }
    }

    private fun applySpeed(clip: Clip, speed: Float) {
        runCatching {
            val control = clip.getControl(FloatControl.Type.SAMPLE_RATE) as? FloatControl ?: return
            val baseRate = clip.format.sampleRate
            val target = (baseRate * speed).coerceIn(control.minimum, control.maximum)
            control.value = target
        }
    }

    private fun startPositionUpdates() {
        if (positionJob?.isActive == true) return
        positionJob = scope.launch {
            while (true) {
                val c = clip ?: break
                _playbackState.update {
                    it.copy(
                        positionMs = c.microsecondPosition / 1000,
                        durationMs = c.microsecondLength / 1000
                    )
                }
                delay(100)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionJob?.cancel()
        positionJob = null
    }

    private fun pcmToWav(
        pcm: ByteArray,
        sampleRate: Int,
        channels: Int = 1,
        bitsPerSample: Int = 16
    ): ByteArray {
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val out = ByteArrayOutputStream()
        with(out) {
            write("RIFF".toByteArray())
            write(intToBytes(36 + pcm.size))
            write("WAVE".toByteArray())
            write("fmt ".toByteArray())
            write(intToBytes(16))
            write(shortToBytes(1))
            write(shortToBytes(channels.toShort()))
            write(intToBytes(sampleRate))
            write(intToBytes(byteRate))
            write(shortToBytes((channels * bitsPerSample / 8).toShort()))
            write(shortToBytes(bitsPerSample.toShort()))
            write("data".toByteArray())
            write(intToBytes(pcm.size))
            write(pcm)
        }
        return out.toByteArray()
    }

    private fun intToBytes(value: Int) = byteArrayOf(
        (value and 0xFF).toByte(),
        ((value shr 8) and 0xFF).toByte(),
        ((value shr 16) and 0xFF).toByte(),
        ((value shr 24) and 0xFF).toByte()
    )

    private fun shortToBytes(value: Short) = byteArrayOf(
        (value.toInt() and 0xFF).toByte(),
        ((value.toInt() shr 8) and 0xFF).toByte()
    )
}
