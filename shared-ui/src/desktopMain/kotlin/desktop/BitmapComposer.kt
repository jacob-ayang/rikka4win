package me.rerere.rikkahub.ui.components.ui

import android.app.Activity
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.CoroutineScope

val LocalExportContext = staticCompositionLocalOf { false }

class BitmapComposer(@Suppress("UNUSED_PARAMETER") mainScope: CoroutineScope) {
    suspend fun composableToBitmap(
        activity: Activity,
        width: Dp? = null,
        height: Dp? = null,
        screenDensity: Density,
        content: @Composable () -> Unit
    ): Bitmap = Bitmap()
}
