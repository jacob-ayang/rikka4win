package me.rerere.rikkahub.ui.components.richtext

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Mermaid(
    code: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = code,
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .padding(8.dp),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
