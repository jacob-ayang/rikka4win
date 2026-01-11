package me.rerere.rikkahub.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

private val ExtendLightColors = lightExtendColors()
private val ExtendDarkColors = darkExtendColors()
val LocalExtendColors = compositionLocalOf { ExtendLightColors }

val LocalDarkMode = compositionLocalOf { false }

private val AMOLED_DARK_BACKGROUND = Color(0xFF000000)

enum class ColorMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class ThemeSettings(
    val themeId: String,
    val dynamicColor: Boolean,
    val amoledDarkMode: Boolean,
    val colorMode: ColorMode,
)

@Composable
fun RikkahubTheme(
    settings: ThemeSettings,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (settings.colorMode) {
        ColorMode.SYSTEM -> isSystemInDarkTheme()
        ColorMode.LIGHT -> false
        ColorMode.DARK -> true
    }

    val dynamicScheme = if (settings.dynamicColor) {
        platformDynamicColorScheme(darkTheme)
    } else {
        null
    }

    val colorScheme = when {
        dynamicScheme != null -> dynamicScheme
        darkTheme -> findPresetTheme(settings.themeId).getColorScheme(dark = true)
        else -> findPresetTheme(settings.themeId).getColorScheme(dark = false)
    }

    val colorSchemeConverted = remember(darkTheme, settings.amoledDarkMode, colorScheme) {
        if (darkTheme && settings.amoledDarkMode) {
            colorScheme.copy(
                background = AMOLED_DARK_BACKGROUND,
                surface = AMOLED_DARK_BACKGROUND,
            )
        } else {
            colorScheme
        }
    }
    val extendColors = if (darkTheme) ExtendDarkColors else ExtendLightColors

    platformUpdateSystemBars(darkTheme)

    CompositionLocalProvider(
        LocalDarkMode provides darkTheme,
        LocalExtendColors provides extendColors,
    ) {
        MaterialTheme(
            colorScheme = colorSchemeConverted,
            typography = Typography,
            content = content,
        )
    }
}

val MaterialTheme.extendColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendColors.current
