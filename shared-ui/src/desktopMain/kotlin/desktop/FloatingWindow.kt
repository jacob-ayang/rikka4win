package me.rerere.rikkahub.ui.components.ui

import androidx.compose.runtime.Composable

@Composable
fun FloatingWindow(
    tag: String,
    visibility: Boolean = true,
    content: @Composable () -> Unit,
) {
    if (visibility) {
        content()
    }
}
