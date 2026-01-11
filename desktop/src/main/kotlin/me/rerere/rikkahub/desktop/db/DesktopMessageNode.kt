package me.rerere.rikkahub.desktop.db

data class DesktopMessageNode(
    val id: String,
    val nodeIndex: Int,
    val selectIndex: Int,
    val messages: List<DisplayMessage>,
)
