package me.rerere.rikkahub.data.ai.transformers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.rerere.ai.ui.UIMessage
import me.rerere.ai.ui.UIMessagePart
import java.io.File

object DocumentAsPromptTransformer : InputMessageTransformer {
    override suspend fun transform(
        ctx: TransformerContext,
        messages: List<UIMessage>,
    ): List<UIMessage> {
        return withContext(Dispatchers.IO) {
            messages.map { message ->
                val parts = message.parts.toMutableList()
                val documents = parts.filterIsInstance<UIMessagePart.Document>()
                if (documents.isEmpty()) {
                    message
                } else {
                    documents.forEach { document ->
                        val file = File(document.url.removePrefix("file:"))
                        val content = runCatching { file.readText() }.getOrNull().orEmpty()
                        if (content.isNotBlank()) {
                            val prompt = """
## user sent a file: ${document.fileName}
<content>
```
$content
```
</content>
""".trimIndent()
                            parts.add(0, UIMessagePart.Text(prompt))
                        }
                    }
                    message.copy(parts = parts)
                }
            }
        }
    }
}
