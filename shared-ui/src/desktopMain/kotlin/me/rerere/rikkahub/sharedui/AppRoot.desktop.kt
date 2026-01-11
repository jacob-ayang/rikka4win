package me.rerere.rikkahub.sharedui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import me.rerere.rikkahub.ui.theme.RikkahubTheme
import me.rerere.rikkahub.ui.theme.ThemeSettings

@Composable
actual fun AppRoot(settings: ThemeSettings) {
    val navController = rememberNavController()
    RikkahubTheme(settings) {
        AppRoutes(navController)
    }
}
