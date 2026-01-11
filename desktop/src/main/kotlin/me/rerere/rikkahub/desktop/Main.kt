package me.rerere.rikkahub.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import me.rerere.rikkahub.sharedui.AppRoot

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "RikkaHub",
    ) {
        AppRoot()
    }
}
