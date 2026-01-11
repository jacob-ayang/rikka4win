package me.rerere.rikkahub.ui.components.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.rerere.rikkahub.utils.Emoji

@Composable
fun EmojiPicker(
    modifier: Modifier = Modifier,
    onEmojiSelected: (Emoji) -> Unit = {},
    showSearch: Boolean = true,
    height: Int = 400
) {
    Text(text = "Emoji Picker", modifier = modifier)
}
