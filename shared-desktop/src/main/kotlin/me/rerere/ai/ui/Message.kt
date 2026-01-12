package me.rerere.ai.ui

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.rerere.ai.core.MessageRole
import me.rerere.ai.core.TokenUsage
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class UIMessage(
    val id: Uuid = Uuid.random(),
    val role: MessageRole,
    val parts: List<UIMessagePart>,
    val annotations: List<UIMessageAnnotation> = emptyList(),
    val createdAt: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    val finishedAt: LocalDateTime? = null,
    val modelId: Uuid? = null,
    val usage: TokenUsage? = null,
    val translation: String? = null,
) {
    fun toText(): String {
        return parts.joinToString(separator = "\n") { part ->
            when (part) {
                is UIMessagePart.Text -> part.text
                else -> ""
            }
        }
    }

    fun summaryAsText(): String {
        return "[${role.name}]: " + parts.joinToString(separator = "\n") { part ->
            when (part) {
                is UIMessagePart.Text -> part.text
                else -> ""
            }
        }
    }

    fun getToolCalls() = parts.filterIsInstance<UIMessagePart.ToolCall>()

    fun getToolResults() = parts.filterIsInstance<UIMessagePart.ToolResult>()

    fun isValidToUpload() = parts.any { it !is UIMessagePart.Reasoning }

    inline fun <reified P : UIMessagePart> hasPart(): Boolean {
        return parts.any { it is P }
    }

    fun hasBase64Part(): Boolean = parts.any {
        it is UIMessagePart.Image && it.url.startsWith("data:")
    }

    companion object {
        fun system(prompt: String) = UIMessage(
            role = MessageRole.SYSTEM,
            parts = listOf(UIMessagePart.Text(prompt))
        )

        fun user(prompt: String) = UIMessage(
            role = MessageRole.USER,
            parts = listOf(UIMessagePart.Text(prompt))
        )

        fun assistant(prompt: String) = UIMessage(
            role = MessageRole.ASSISTANT,
            parts = listOf(UIMessagePart.Text(prompt))
        )
    }
}

@Serializable
sealed class UIMessagePart {
    abstract val priority: Int
    abstract val metadata: JsonObject?

    @Serializable
    data class Text(
        val text: String,
        override var metadata: JsonObject? = null,
    ) : UIMessagePart() {
        override val priority: Int = 0
    }

    @Serializable
    data class Image(
        val url: String,
        override var metadata: JsonObject? = null,
    ) : UIMessagePart() {
        override val priority: Int = 1
    }

    @Serializable
    data class Video(
        val url: String,
        override var metadata: JsonObject? = null,
    ) : UIMessagePart() {
        override val priority: Int = 1
    }

    @Serializable
    data class Audio(
        val url: String,
        override var metadata: JsonObject? = null,
    ) : UIMessagePart() {
        override val priority: Int = 1
    }

    @Serializable
    data class Document(
        val url: String,
        val fileName: String,
        val mime: String = "text/*",
        override var metadata: JsonObject? = null,
    ) : UIMessagePart() {
        override val priority: Int = 1
    }

    @Serializable
    data class Reasoning(
        val reasoning: String,
        val createdAt: Instant = Clock.System.now(),
        val finishedAt: Instant? = Clock.System.now(),
        override var metadata: JsonObject? = null,
    ) : UIMessagePart() {
        override val priority: Int = -1
    }

    @Serializable
    @SerialName("search")
    data object Search : UIMessagePart() {
        override val priority: Int = 0
        override var metadata: JsonObject? = null
    }

    @Serializable
    data class ToolCall(
        val toolCallId: String,
        val toolName: String,
        val arguments: String,
        override var metadata: JsonObject? = null,
    ) : UIMessagePart() {
        override val priority: Int = 0
    }

    @Serializable
    data class ToolResult(
        val toolCallId: String,
        val toolName: String,
        val content: JsonElement,
        val arguments: JsonElement,
        override var metadata: JsonObject? = null,
    ) : UIMessagePart() {
        override val priority: Int = 0
    }
}

@Serializable
sealed class UIMessageAnnotation {
    @Serializable
    @SerialName("url_citation")
    data class UrlCitation(
        val title: String,
        val url: String,
    ) : UIMessageAnnotation()
}

fun List<UIMessagePart>.isEmptyUIMessage(): Boolean {
    return isEmpty() || all { it is UIMessagePart.Reasoning }
}

fun List<UIMessagePart>.isEmptyInputMessage(): Boolean {
    if (isEmpty()) return true
    return all { part ->
        part is UIMessagePart.Reasoning
    }
}

fun List<UIMessage>.truncate(index: Int): List<UIMessage> {
    if (index < 0 || index > lastIndex) return this
    return subList(index, size)
}

fun UIMessage.finishReasoning(): UIMessage {
    return copy(
        parts = parts.map { part ->
            if (part is UIMessagePart.Reasoning && part.finishedAt == null) {
                part.copy(finishedAt = Clock.System.now())
            } else {
                part
            }
        }
    )
}
