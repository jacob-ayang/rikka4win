package me.rerere.highlight

import androidx.compose.runtime.staticCompositionLocalOf

class Highlighter {
    suspend fun highlight(code: String, language: String?): List<HighlightToken> {
        return listOf(HighlightToken.Plain(code))
    }
}

val LocalHighlighter = staticCompositionLocalOf { Highlighter() }
