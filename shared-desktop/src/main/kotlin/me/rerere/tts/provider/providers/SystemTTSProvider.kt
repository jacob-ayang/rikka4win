package me.rerere.tts.provider.providers

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import me.rerere.common.android.appTempFolder
import me.rerere.tts.model.AudioChunk
import me.rerere.tts.model.AudioFormat
import me.rerere.tts.model.TTSRequest
import me.rerere.tts.provider.TTSProvider
import me.rerere.tts.provider.TTSProviderSetting
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Base64

private const val TAG = "SystemTTSProvider"

class SystemTTSProvider : TTSProvider<TTSProviderSetting.SystemTTS> {
    override fun generateSpeech(
        context: Context,
        providerSetting: TTSProviderSetting.SystemTTS,
        request: TTSRequest
    ): Flow<AudioChunk> = flow {
        val audioData = withContext(Dispatchers.IO) {
            val tempDir = context.appTempFolder
            val audioFile = File(tempDir, "tts_${System.currentTimeMillis()}.wav")
            if (audioFile.exists()) {
                audioFile.delete()
            }

            val ok = when {
                isWindows() -> synthesizeWithPowerShell(audioFile, request.text, providerSetting.speechRate)
                isMac() -> synthesizeWithSay(audioFile, request.text, providerSetting.speechRate)
                else -> synthesizeWithEspeak(audioFile, request.text, providerSetting.speechRate)
            }

            if (!ok || !audioFile.exists()) {
                throw IllegalStateException("System TTS synthesis failed")
            }

            val bytes = audioFile.readBytes()
            audioFile.delete()
            bytes
        }

        emit(
            AudioChunk(
                data = audioData,
                format = AudioFormat.WAV,
                isLast = true,
                metadata = mapOf(
                    "provider" to "system",
                    "speechRate" to providerSetting.speechRate.toString(),
                    "pitch" to providerSetting.pitch.toString()
                )
            )
        )
    }

    private fun synthesizeWithPowerShell(output: File, text: String, speechRate: Float): Boolean {
        val encoded = Base64.getEncoder().encodeToString(text.toByteArray(StandardCharsets.UTF_8))
        val rate = ((speechRate - 1.0f) * 5).toInt().coerceIn(-10, 10)
        val command = listOf(
            "powershell",
            "-NoProfile",
            "-Command",
            "\$t=[System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String('$encoded'));" +
                "\$s=New-Object System.Speech.Synthesis.SpeechSynthesizer;" +
                "\$s.Rate=$rate;" +
                "\$s.SetOutputToWaveFile('${output.absolutePath}');" +
                "\$s.Speak(\$t);" +
                "\$s.Dispose();"
        )
        return runProcess(command)
    }

    private fun synthesizeWithSay(output: File, text: String, speechRate: Float): Boolean {
        val rate = (175 * speechRate).toInt().coerceIn(80, 500)
        val command = listOf(
            "say",
            "-r",
            rate.toString(),
            "--data-format=LEI16@24000",
            "-o",
            output.absolutePath,
            text
        )
        return runProcess(command)
    }

    private fun synthesizeWithEspeak(output: File, text: String, speechRate: Float): Boolean {
        val rate = (175 * speechRate).toInt().coerceIn(80, 450)
        val command = listOf(
            "espeak",
            "-s",
            rate.toString(),
            "-w",
            output.absolutePath,
            text
        )
        return runProcess(command)
    }

    private fun runProcess(command: List<String>): Boolean {
        return runCatching {
            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText()
            val exit = process.waitFor()
            if (exit != 0) {
                Log.e(TAG, "System TTS command failed ($exit): $output")
            }
            exit == 0
        }.getOrElse {
            Log.e(TAG, "System TTS command failed", it)
            false
        }
    }

    private fun isWindows(): Boolean =
        System.getProperty("os.name")?.lowercase()?.contains("win") == true

    private fun isMac(): Boolean =
        System.getProperty("os.name")?.lowercase()?.contains("mac") == true
}
