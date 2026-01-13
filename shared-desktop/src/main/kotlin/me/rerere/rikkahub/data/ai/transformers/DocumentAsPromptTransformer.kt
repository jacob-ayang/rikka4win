package me.rerere.rikkahub.data.ai.transformers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.rerere.ai.ui.UIMessage
import me.rerere.ai.ui.UIMessagePart
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.apache.poi.xslf.usermodel.XSLFTextShape
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileInputStream

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
                        val content = when (document.mime) {
                            "application/pdf" -> parsePdfAsText(file)
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ->
                                parseDocxAsText(file)
                            "application/vnd.openxmlformats-officedocument.presentationml.presentation" ->
                                parsePptxAsText(file)
                            else -> runCatching { file.readText() }.getOrNull().orEmpty()
                        }
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

    private fun parsePdfAsText(file: File): String = runCatching {
        PDDocument.load(file).use { document ->
            PDFTextStripper().getText(document)
        }
    }.getOrElse {
        runCatching { file.readText() }.getOrNull().orEmpty()
    }

    private fun parseDocxAsText(file: File): String = runCatching {
        FileInputStream(file).use { stream ->
            XWPFDocument(stream).use { doc ->
                doc.paragraphs.joinToString("\n") { it.text }
            }
        }
    }.getOrElse {
        runCatching { file.readText() }.getOrNull().orEmpty()
    }

    private fun parsePptxAsText(file: File): String = runCatching {
        FileInputStream(file).use { stream ->
            XMLSlideShow(stream).use { show ->
                show.slides.joinToString("\n") { slide ->
                    slide.shapes.filterIsInstance<XSLFTextShape>()
                        .joinToString("\n") { it.text }
                }
            }
        }
    }.getOrElse {
        runCatching { file.readText() }.getOrNull().orEmpty()
    }
}
