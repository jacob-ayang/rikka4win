package me.rerere.rikkahub.utils

data class Emoji(
    val emoji: String = "",
    val name: String = ""
)

data class EmojiData(
    val categories: List<EmojiCategory> = emptyList()
)

data class EmojiCategory(
    val name: String = "",
    val emojis: List<Emoji> = emptyList()
) {
    fun getEmojiVariants(): List<Pair<Emoji, List<Emoji>>> {
        return emojis.map { it to listOf(it) }
    }
}
