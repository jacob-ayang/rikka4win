package me.rerere.rikkahub.desktop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class DesktopSection {
    CHAT,
    HISTORY,
    PROVIDERS,
    BACKUP,
}

@Composable
fun NavPanel(
    active: DesktopSection,
    onSelect: (DesktopSection) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SectionButton("Chat", active == DesktopSection.CHAT) { onSelect(DesktopSection.CHAT) }
        SectionButton("History", active == DesktopSection.HISTORY) { onSelect(DesktopSection.HISTORY) }
        SectionButton("Providers", active == DesktopSection.PROVIDERS) { onSelect(DesktopSection.PROVIDERS) }
        SectionButton("Backup", active == DesktopSection.BACKUP) { onSelect(DesktopSection.BACKUP) }
    }
}

@Composable
private fun SectionButton(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = if (selected) {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        } else {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
            )
        },
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
