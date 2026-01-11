package me.rerere.rikkahub.data.ai.transformers

import me.rerere.ai.ui.UIMessage

class TemplateTransformer : InputMessageTransformer {
    override suspend fun transform(
        ctx: TransformerContext,
        messages: List<UIMessage>,
    ): List<UIMessage> = messages
}
