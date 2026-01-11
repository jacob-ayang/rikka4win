package me.rerere.rikkahub.data.ai.transformers

import android.content.Context
import me.rerere.ai.provider.Model
import me.rerere.ai.ui.UIMessage
import me.rerere.rikkahub.data.datastore.Settings
import me.rerere.rikkahub.data.model.Assistant

class TransformerContext(
    val context: Context,
    val model: Model,
    val assistant: Assistant,
    val settings: Settings,
)

interface MessageTransformer {
    suspend fun transform(
        ctx: TransformerContext,
        messages: List<UIMessage>,
    ): List<UIMessage> = messages
}

interface InputMessageTransformer : MessageTransformer

interface OutputMessageTransformer : MessageTransformer
