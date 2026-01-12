package me.rerere.rikkahub.data.ai.transformers

import me.rerere.ai.core.MessageRole
import me.rerere.ai.ui.UIMessage
import me.rerere.ai.ui.UIMessagePart
import me.rerere.rikkahub.data.model.Assistant
import me.rerere.rikkahub.data.model.InjectionPosition
import me.rerere.rikkahub.data.model.Lorebook
import me.rerere.rikkahub.data.model.PromptInjection
import me.rerere.rikkahub.data.model.extractContextForMatching
import me.rerere.rikkahub.data.model.isTriggered

object PromptInjectionTransformer : InputMessageTransformer {
    override suspend fun transform(
        ctx: TransformerContext,
        messages: List<UIMessage>,
    ): List<UIMessage> {
        return transformMessages(
            messages = messages,
            assistant = ctx.assistant,
            modeInjections = ctx.settings.modeInjections,
            lorebooks = ctx.settings.lorebooks
        )
    }
}

internal fun transformMessages(
    messages: List<UIMessage>,
    assistant: Assistant,
    modeInjections: List<PromptInjection.ModeInjection>,
    lorebooks: List<Lorebook>
): List<UIMessage> {
    val injections = collectInjections(
        messages = messages,
        assistant = assistant,
        modeInjections = modeInjections,
        lorebooks = lorebooks
    )

    if (injections.isEmpty()) {
        return messages
    }

    val byPosition = injections
        .sortedByDescending { it.priority }
        .groupBy { it.position }

    return applyInjections(messages, byPosition)
}

internal fun collectInjections(
    messages: List<UIMessage>,
    assistant: Assistant,
    modeInjections: List<PromptInjection.ModeInjection>,
    lorebooks: List<Lorebook>
): List<PromptInjection> {
    val injections = mutableListOf<PromptInjection>()

    modeInjections
        .filter { it.enabled && assistant.modeInjectionIds.contains(it.id) }
        .forEach { injections.add(it) }

    val enabledLorebooks = lorebooks.filter {
        it.enabled && assistant.lorebookIds.contains(it.id)
    }
    if (enabledLorebooks.isNotEmpty()) {
        val nonSystemMessages = messages.filter { it.role != MessageRole.SYSTEM }

        enabledLorebooks.forEach { lorebook ->
            lorebook.entries
                .filter { entry ->
                    val context = extractContextForMatching(nonSystemMessages, entry.scanDepth)
                    entry.isTriggered(context)
                }
                .forEach { injections.add(it) }
        }
    }

    return injections
}

internal fun applyInjections(
    messages: List<UIMessage>,
    byPosition: Map<InjectionPosition, List<PromptInjection>>
): List<UIMessage> {
    val result = messages.toMutableList()

    val systemIndex = result.indexOfFirst { it.role == MessageRole.SYSTEM }

    if (systemIndex >= 0) {
        val beforeContent = byPosition[InjectionPosition.BEFORE_SYSTEM_PROMPT]
            ?.joinToString("\n") { it.content } ?: ""
        val afterContent = byPosition[InjectionPosition.AFTER_SYSTEM_PROMPT]
            ?.joinToString("\n") { it.content } ?: ""

        if (beforeContent.isNotEmpty() || afterContent.isNotEmpty()) {
            val systemMessage = result[systemIndex]
            val originalText = systemMessage.parts
                .filterIsInstance<UIMessagePart.Text>()
                .joinToString("") { it.text }

            val newText = buildString {
                if (beforeContent.isNotEmpty()) {
                    append(beforeContent)
                    appendLine()
                }
                append(originalText)
                if (afterContent.isNotEmpty()) {
                    appendLine()
                    append(afterContent)
                }
            }

            result[systemIndex] = systemMessage.copy(
                parts = listOf(UIMessagePart.Text(newText))
            )
        }
    } else {
        val beforeContent = byPosition[InjectionPosition.BEFORE_SYSTEM_PROMPT]
            ?.joinToString("\n") { it.content } ?: ""
        val afterContent = byPosition[InjectionPosition.AFTER_SYSTEM_PROMPT]
            ?.joinToString("\n") { it.content } ?: ""

        val combinedContent = buildString {
            if (beforeContent.isNotEmpty()) {
                append(beforeContent)
            }
            if (afterContent.isNotEmpty()) {
                if (isNotEmpty()) appendLine()
                append(afterContent)
            }
        }

        if (combinedContent.isNotEmpty()) {
            result.add(0, UIMessage.system(combinedContent))
        }
    }

    val topContent = byPosition[InjectionPosition.TOP_OF_CHAT]
        ?.joinToString("\n") { it.content }
    if (!topContent.isNullOrEmpty()) {
        var insertIndex = result.indexOfFirst { it.role == MessageRole.USER }
            .takeIf { it >= 0 } ?: result.size
        insertIndex = findSafeInsertIndex(result, insertIndex)
        result.add(insertIndex, UIMessage.user(wrapSystemTag(topContent)))
    }

    val bottomContent = byPosition[InjectionPosition.BOTTOM_OF_CHAT]
        ?.joinToString("\n") { it.content }
    if (!bottomContent.isNullOrEmpty()) {
        var insertIndex = (result.size - 1).coerceAtLeast(0)
        insertIndex = findSafeInsertIndex(result, insertIndex)
        result.add(insertIndex, UIMessage.user(wrapSystemTag(bottomContent)))
    }

    val atDepthInjections = byPosition[InjectionPosition.AT_DEPTH]
    if (!atDepthInjections.isNullOrEmpty()) {
        val byDepth = atDepthInjections.groupBy { it.injectDepth }
        byDepth.keys.sortedDescending().forEach { depth ->
            val content = byDepth[depth]?.joinToString("\n") { it.content } ?: return@forEach
            var insertIndex = (result.size - depth).coerceIn(0, result.size)
            insertIndex = findSafeInsertIndex(result, insertIndex)
            result.add(insertIndex, UIMessage.user(wrapSystemTag(content)))
        }
    }

    return result
}

private fun wrapSystemTag(content: String): String {
    return """
<system>
$content
</system>
""".trimIndent()
}

private fun findSafeInsertIndex(messages: List<UIMessage>, index: Int): Int {
    if (messages.isEmpty()) return 0
    if (index <= 0) return 0
    if (index >= messages.size) return messages.size

    var insertIndex = index
    val targetMessage = messages[insertIndex]

    if (targetMessage.role == MessageRole.SYSTEM) {
        insertIndex++
    }

    if (insertIndex >= messages.size) insertIndex = messages.size
    return insertIndex
}
