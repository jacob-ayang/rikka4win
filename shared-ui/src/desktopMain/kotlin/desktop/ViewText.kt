package me.rerere.rikkahub.ui.components.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

@Composable
fun ViewText(
    text: CharSequence,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
) {
    Text(text = text.toString(), modifier = modifier, style = style)
}
