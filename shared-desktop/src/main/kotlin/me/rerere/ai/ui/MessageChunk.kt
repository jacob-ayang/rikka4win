package me.rerere.ai.ui

import kotlinx.serialization.Serializable
import me.rerere.ai.core.TokenUsage

@Serializable
data class MessageChunk(
    val id: String = "",
    val model: String = "",
    val choices: List<UIMessageChoice> = emptyList(),
    val usage: TokenUsage? = null,
)

@Serializable
data class UIMessageChoice(
    val index: Int = 0,
    val delta: UIMessage? = null,
    val message: UIMessage? = null,
    val finishReason: String? = null,
)
