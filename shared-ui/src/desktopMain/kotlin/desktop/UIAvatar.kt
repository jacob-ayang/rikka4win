package me.rerere.rikkahub.ui.components.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import me.rerere.rikkahub.data.model.Avatar

@Composable
fun TextAvatar(
    text: String,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    color: Color = Color.Unspecified,
) {
    Text(text = text.take(2), modifier = modifier)
}

@Composable
fun UIAvatar(
    name: String,
    value: Avatar,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    onUpdate: ((Avatar) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    TextAvatar(text = name, modifier = modifier, loading = loading)
}
