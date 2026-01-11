package me.rerere.rikkahub.sharedui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.rerere.rikkahub.ui.theme.RikkahubTheme
import me.rerere.rikkahub.ui.theme.ThemeSettings

@Composable
fun AppRoot(settings: ThemeSettings) {
    RikkahubTheme(settings) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = "RikkaHub Desktop",
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = "Shared UI scaffolding is ready. Migration starts next.",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
