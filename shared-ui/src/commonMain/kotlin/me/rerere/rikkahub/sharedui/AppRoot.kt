package me.rerere.rikkahub.sharedui

import androidx.compose.runtime.Composable
import me.rerere.rikkahub.ui.theme.ThemeSettings

@Composable
expect fun AppRoot(settings: ThemeSettings)
