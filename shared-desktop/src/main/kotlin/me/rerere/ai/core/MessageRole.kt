package me.rerere.ai.core

import kotlinx.serialization.Serializable

@Serializable
enum class MessageRole {
    SYSTEM,
    USER,
    ASSISTANT,
    TOOL,
}
