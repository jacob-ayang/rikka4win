package me.rerere.rikkahub.ui.components.ui

import androidx.compose.runtime.Composable

@Composable
fun ImagePreviewDialog(
    images: List<String>,
    onDismissRequest: () -> Unit,
) {
    onDismissRequest()
}
