package androidx.compose.ui.res

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import me.rerere.rikkahub.DesktopResources

@Composable
fun stringResource(@StringRes id: Int, vararg args: Any): String {
    val raw = DesktopResources.strings[id] ?: "string:$id"
    return if (args.isNotEmpty()) raw.format(*args) else raw
}

@Composable
fun painterResource(@DrawableRes id: Int): Painter {
    return ColorPainter(Color.Transparent)
}
