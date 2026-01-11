package me.rerere.rikkahub.desktop.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun RikkahubDesktopTheme(
    themeId: String = "sakura",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val scheme = findPresetTheme(themeId).scheme(darkTheme)
    MaterialTheme(
        colorScheme = scheme,
        content = content,
    )
}
