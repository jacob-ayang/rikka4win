package me.rerere.highlight

sealed class HighlightToken {
    data class Plain(
        val content: String,
    ) : HighlightToken()

    sealed class Token : HighlightToken() {
        abstract val length: Int

        data class StringContent(
            val content: String,
            val type: String = "",
            override val length: Int = content.length,
        ) : Token()

        data class StringListContent(
            val content: List<String>,
            val type: String = "",
            override val length: Int = content.sumOf { it.length },
        ) : Token()

        data class Nested(
            val content: List<Token>,
            val type: String = "",
            override val length: Int = content.sumOf { it.length },
        ) : Token()
    }
}
