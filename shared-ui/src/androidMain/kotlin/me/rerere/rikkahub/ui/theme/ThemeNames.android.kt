package me.rerere.rikkahub.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import me.rerere.rikkahub.sharedui.R

@Composable
actual fun themeName(key: ThemeNameKey, fallback: String): String {
    val resId = when (key) {
        ThemeNameKey.SAKURA -> R.string.theme_name_sakura
        ThemeNameKey.OCEAN -> R.string.theme_name_ocean
        ThemeNameKey.SPRING -> R.string.theme_name_spring
        ThemeNameKey.AUTUMN -> R.string.theme_name_autumn
        ThemeNameKey.BLACK -> R.string.theme_name_black
    }
    return stringResource(id = resId)
}
