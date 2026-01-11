package me.rerere.rikkahub.data.ai.transformers

import androidx.compose.runtime.Composable

data class PlaceholderInfo(
    val displayName: @Composable () -> Unit = {},
    val resolver: (Any) -> String = { "" }
)

interface PlaceholderProvider {
    val placeholders: Map<String, PlaceholderInfo>
}

object DefaultPlaceholderProvider : PlaceholderProvider {
    override val placeholders: Map<String, PlaceholderInfo> = emptyMap()
}
