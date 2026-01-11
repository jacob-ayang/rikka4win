package me.rerere.rikkahub.ui.theme

import androidx.compose.runtime.Composable

enum class ThemeNameKey {
    SAKURA,
    OCEAN,
    SPRING,
    AUTUMN,
    BLACK,
}

@Composable
expect fun themeName(key: ThemeNameKey, fallback: String): String
