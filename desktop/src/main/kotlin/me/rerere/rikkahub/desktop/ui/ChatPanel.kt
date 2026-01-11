package me.rerere.rikkahub.desktop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import me.rerere.rikkahub.desktop.db.ConversationSummary
import me.rerere.rikkahub.desktop.db.DisplayMessage
import me.rerere.rikkahub.desktop.db.MessageContent
import me.rerere.rikkahub.desktop.db.DesktopMessageNode
import androidx.compose.material3.OutlinedTextField

@Composable
fun ChatPanel(
    selectedConversation: ConversationSummary?,
    messages: List<DisplayMessage>,
    nodes: List<DesktopMessageNode>,
    onSelectBranch: (DesktopMessageNode, Int) -> Unit,
) {
    val (draft, setDraft) = remember { mutableStateOf("") }
    val displayNodes = if (nodes.isNotEmpty()) {
        nodes
    } else {
        messages.mapIndexed { index, message ->
            DesktopMessageNode(
                id = "",
                nodeIndex = index,
                selectIndex = 0,
                messages = listOf(message),
            )
        }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Chat",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = selectedConversation?.title ?: "Select a conversation to view it here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (selectedConversation != null) {
                if (displayNodes.isEmpty()) {
                    Text(
                        text = "No messages loaded yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    displayNodes.forEach { node ->
                        val safeIndex = node.selectIndex.coerceIn(node.messages.indices)
                        val message = node.messages.getOrNull(safeIndex) ?: node.messages.firstOrNull()
                        if (message != null) {
                            if (node.messages.size > 1 && node.id.isNotBlank()) {
                                BranchSelector(
                                    node = node,
                                    onSelect = { nextIndex -> onSelectBranch(node, nextIndex) },
                                )
                            }
                            val colors = messageColorsForRole(message.role)
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = colors.container,
                                    contentColor = colors.content,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(
                                        text = message.role.ifBlank { "Unknown" },
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colors.content,
                                    )
                                    message.contents.forEach { content ->
                                        when (content) {
                                            is MessageContent.Text -> Text(
                                                text = content.value,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = colors.content,
                                            )
                                            is MessageContent.Reasoning -> Text(
                                                text = "Reasoning: ${content.value}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontStyle = FontStyle.Italic,
                                            )
                                            is MessageContent.Tool -> Text(
                                                text = "Tool: ${content.name}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                            is MessageContent.Media -> Text(
                                                text = "Media: ${content.url}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = draft,
                    onValueChange = setDraft,
                    label = { Text("Type a message") },
                    singleLine = true,
                )
                Button(
                    onClick = { setDraft("") },
                    enabled = draft.isNotBlank(),
                ) {
                    Text("Send")
                }
            }
        }
    }
}

@Composable
private fun BranchSelector(
    node: DesktopMessageNode,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Branch ${node.selectIndex + 1}/${node.messages.size}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(
                onClick = { onSelect(node.selectIndex - 1) },
                enabled = node.selectIndex > 0,
            ) {
                Text("Prev")
            }
            TextButton(
                onClick = { onSelect(node.selectIndex + 1) },
                enabled = node.selectIndex < node.messages.lastIndex,
            ) {
                Text("Next")
            }
        }
    }
}

@Composable
private fun messageColorsForRole(role: String): RoleColors {
    val scheme = MaterialTheme.colorScheme
    return when (role.lowercase()) {
        "user" -> RoleColors(
            container = scheme.primaryContainer,
            content = scheme.onPrimaryContainer,
        )
        "assistant" -> RoleColors(
            container = scheme.surfaceContainerHigh,
            content = scheme.onSurface,
        )
        "system" -> RoleColors(
            container = scheme.secondaryContainer,
            content = scheme.onSecondaryContainer,
        )
        "tool" -> RoleColors(
            container = scheme.tertiaryContainer,
            content = scheme.onTertiaryContainer,
        )
        else -> RoleColors(
            container = scheme.surfaceVariant,
            content = scheme.onSurfaceVariant,
        )
    }
}

private data class RoleColors(
    val container: Color,
    val content: Color,
)
