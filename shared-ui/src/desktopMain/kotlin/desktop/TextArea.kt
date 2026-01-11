package me.rerere.rikkahub.ui.components.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.input.TextFieldState

@Composable
fun TextArea(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
    minLines: Int = 5,
    maxLines: Int = 10,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    supportedFileTypes: Array<String> = arrayOf("text/*", "application/json"),
    enableFullscreen: Boolean = true,
    onImportError: ((String) -> Unit)? = null
) {
    Text(text = label.ifEmpty { placeholder }, modifier = modifier)
}
