package me.rerere.rikkahub.ui.pages.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dokar.sonner.ToastType
import me.rerere.ai.core.MessageRole
import me.rerere.ai.ui.UIMessage
import me.rerere.ai.ui.UIMessagePart
import me.rerere.ai.ui.toSortedMessageParts
import me.rerere.ai.util.encodeBase64
import me.rerere.rikkahub.R
import me.rerere.rikkahub.data.model.Conversation
import me.rerere.rikkahub.ui.context.LocalToaster
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ImageExportOptions(val expandReasoning: Boolean = false)

@Composable
fun ChatExportSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    conversation: Conversation,
    selectedMessages: List<UIMessage>,
) {
    val toaster = LocalToaster.current
    val markdownSuccessMessage = stringResource(id = R.string.chat_page_export_success, "Markdown")
    if (!visible) return

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = stringResource(id = R.string.chat_page_export_format))

            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    runCatching {
                        exportToMarkdown(conversation, selectedMessages)
                        toaster.show(
                            message = markdownSuccessMessage,
                            type = ToastType.Success
                        )
                    }.onFailure {
                        toaster.show(
                            message = it.message ?: "Export failed",
                            type = ToastType.Error
                        )
                    }
                    onDismissRequest()
                }
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(id = R.string.chat_page_export_markdown)) },
                    supportingContent = { Text(stringResource(id = R.string.chat_page_export_markdown_desc)) }
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    toaster.show(
                        message = "Image export is not available on desktop yet.",
                        type = ToastType.Info
                    )
                }
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(id = R.string.chat_page_export_image)) },
                    supportingContent = { Text(stringResource(id = R.string.chat_page_export_image_desc)) }
                )
            }
        }
    }
}

private fun exportToMarkdown(
    conversation: Conversation,
    messages: List<UIMessage>
) {
    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
    val fileName = "chat-export-$timestamp.md"
    val target = pickSaveFile("Export Markdown", fileName) ?: return

    val content = buildString {
        append("# ${conversation.title}\n\n")
        append("*Exported on ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}*\n\n")

        messages.forEach { message ->
            val role = when (message.role) {
                MessageRole.USER -> "**User**"
                MessageRole.SYSTEM -> "**System**"
                else -> "**Assistant**"
            }
            append("$role:\n\n")
            message.parts.toSortedMessageParts().forEach { part ->
                when (part) {
                    is UIMessagePart.Text -> {
                        append(part.text)
                        appendLine()
                    }

                    is UIMessagePart.Image -> {
                        val encoded = part.encodeBase64().getOrNull()
                        if (encoded != null) {
                            append("![Image]($encoded)")
                        } else {
                            append("[Image]")
                        }
                        appendLine()
                    }

                    is UIMessagePart.ToolCall -> {
                        append("Tool call: ${part.toolName}\n")
                    }

                    is UIMessagePart.ToolResult -> {
                        append("Tool result: ${part.toolName}\n")
                    }

                    else -> Unit
                }
            }
            appendLine()
        }
    }

    target.writeText(content)
}

private fun pickSaveFile(title: String, defaultName: String): File? {
    val dialog = FileDialog(null as Frame?, title, FileDialog.SAVE)
    dialog.file = defaultName
    dialog.isVisible = true
    val file = dialog.file ?: return null
    val target = File(dialog.directory, file)
    return if (target.extension.equals("md", ignoreCase = true)) {
        target
    } else {
        File(target.parentFile, "${target.name}.md")
    }
}
