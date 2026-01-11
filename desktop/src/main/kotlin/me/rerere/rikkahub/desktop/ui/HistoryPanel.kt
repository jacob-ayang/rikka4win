package me.rerere.rikkahub.desktop.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.rerere.rikkahub.desktop.db.ConversationSummary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HistoryPanel(
    conversations: List<ConversationSummary>,
    selectedId: String?,
    onSelect: (ConversationSummary) -> Unit,
    onRefresh: () -> Unit,
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                TextButton(onClick = onRefresh) {
                    Text("Refresh")
                }
            }
            Text(
                text = if (conversations.isEmpty()) {
                    "No conversations yet. Database mapping will populate this list."
                } else {
                    "Total: ${conversations.size}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            conversations.forEach { conversation ->
                val isSelected = conversation.id == selectedId
                val timeText = formatTime(conversation.updateAt)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(conversation) }
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (conversation.isPinned) "★ ${conversation.title}" else conversation.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = timeText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

private fun formatTime(epochMillis: Long): String {
    return runCatching {
        val instant = Instant.ofEpochMilli(epochMillis)
        instant.atZone(ZoneId.systemDefault()).format(formatter)
    }.getOrElse { "Unknown time" }
}
