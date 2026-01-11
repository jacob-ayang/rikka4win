package me.rerere.rikkahub.data.ai

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.rerere.rikkahub.data.datastore.Settings
import java.util.Locale

class GenerationHandler {
    fun translateText(
        settings: Settings,
        sourceText: String,
        targetLanguage: Locale,
        onStreamUpdate: ((String) -> Unit)? = null
    ): Flow<String> = flow {
        val translated = sourceText
        onStreamUpdate?.invoke(translated)
        emit(translated)
    }
}
