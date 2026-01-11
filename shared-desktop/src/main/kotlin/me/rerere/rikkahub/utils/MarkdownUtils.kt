package me.rerere.rikkahub.utils

fun String.stripMarkdown(): String {
    return this
        .replace(Regex("```[\\s\\S]*?```|`[^`]*?`"), "")
        .replace(Regex("!?\\[([^\\]]+)\\]\\([^\\)]*\\)"), "$1")
        .replace(Regex("\\*\\*([^*]+?)\\*\\*"), "$1")
        .replace(Regex("\\*([^*]+?)\\*"), "$1")
        .replace(Regex("__([^_]+?)__"), "$1")
        .replace(Regex("_([^_]+?)_"), "$1")
        .replace(Regex("~~([^~]+?)~~"), "$1")
        .replace(Regex("(?m)^#+\\s*"), "")
        .replace(Regex("(?m)^\\s*[-*+]\\s+"), "")
        .replace(Regex("(?m)^\\s*\\d+\\.\\s+"), "")
        .replace(Regex("(?m)^>\\s*"), "")
        .replace(Regex("(?m)^(\\s*[-*_]){3,}\\s*$"), "")
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()
}
