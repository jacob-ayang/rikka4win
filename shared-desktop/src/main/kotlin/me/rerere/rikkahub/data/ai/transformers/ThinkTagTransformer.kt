package me.rerere.rikkahub.data.ai.transformers

import me.rerere.ai.core.MessageRole
import me.rerere.ai.ui.UIMessage
import me.rerere.ai.ui.UIMessagePart
import kotlin.time.Clock

private val THINKING_REGEX = Regex("<think>([\\s\\S]*?)(?:</think>|$)", RegexOption.DOT_MATCHES_ALL)

object ThinkTagTransformer : OutputMessageTransformer {
    override suspend fun visualTransform(
        ctx: TransformerContext,
        messages: List<UIMessage>,
    ): List<UIMessage> {
        return messages.map { message ->
            if (message.role == MessageRole.ASSISTANT && message.hasPart<UIMessagePart.Text>()) {
                message.copy(
                    parts = message.parts.flatMap { part ->
                        if (part is UIMessagePart.Text && part.text.startsWith("<think>")) {
                            val stripped = part.text.replace(THINKING_REGEX, "")
                            val reasoning =
                                THINKING_REGEX.find(part.text)?.groupValues?.getOrNull(1)?.trim()
                                    ?: ""
                            val now = Clock.System.now()
                            listOf(
                                UIMessagePart.Reasoning(
                                    reasoning = reasoning,
                                    finishedAt = now,
                                    createdAt = now,
                                ),
                                part.copy(text = stripped),
                            )
                        } else {
                            listOf(part)
                        }
                    }
                )
            } else {
                message
            }
        }
    }
}
