package me.rerere.rikkahub.data.ai

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class AILogging {
    data class Generation(
        val prompt: String = "",
        val response: String = "",
    ) : AILogging()
}

class AILoggingManager {
    private val logs = MutableStateFlow<List<AILogging>>(emptyList())

    fun getLogs(): StateFlow<List<AILogging>> = logs

    fun addLog(log: AILogging) {
        logs.value = logs.value + log
    }
}
