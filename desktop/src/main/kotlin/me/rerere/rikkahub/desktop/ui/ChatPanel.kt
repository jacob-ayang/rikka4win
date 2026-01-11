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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.rerere.rikkahub.desktop.db.ConversationSummary
import me.rerere.rikkahub.desktop.db.DisplayMessage
import me.rerere.rikkahub.desktop.db.MessageContent
import androidx.compose.material3.OutlinedTextField

@Composable
fun ChatPanel(
    selectedConversation: ConversationSummary?,
    messages: List<DisplayMessage>,
) {
    val (draft, setDraft) = remember { mutableStateOf("") }
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
                if (messages.isEmpty()) {
                    Text(
                        text = "No messages loaded yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    messages.forEach { message ->
                        Text(
                            text = "${message.role}:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        message.contents.forEach { content ->
                            Text(
                                text = when (content) {
                                    is MessageContent.Text -> content.value
                                    is MessageContent.Reasoning -> "Reasoning: ${content.value}"
                                    is MessageContent.Tool -> "Tool: ${content.name}"
                                    is MessageContent.Media -> "Media: ${content.url}"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
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
