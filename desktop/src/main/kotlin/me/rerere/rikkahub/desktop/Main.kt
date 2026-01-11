package me.rerere.rikkahub.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import me.rerere.rikkahub.sharedui.AppRoot
import me.rerere.rikkahub.ui.theme.ColorMode
import me.rerere.rikkahub.ui.theme.ThemeSettings

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "RikkaHub",
    ) {
        val themeSettings = ThemeSettings(
            themeId = "sakura",
            dynamicColor = false,
            amoledDarkMode = false,
            colorMode = ColorMode.SYSTEM,
        )
        AppRoot(themeSettings)
    }
}
