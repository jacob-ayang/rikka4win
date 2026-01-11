package me.rerere.ai.core

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import me.rerere.ai.provider.Model
import me.rerere.ai.ui.UIMessage

data class Tool(
    val name: String,
    val description: String,
    val parameters: () -> InputSchema? = { null },
    val systemPrompt: (model: Model, messages: List<UIMessage>) -> String = { _, _ -> "" },
    val execute: suspend (JsonElement) -> JsonElement = { JsonNull },
)
