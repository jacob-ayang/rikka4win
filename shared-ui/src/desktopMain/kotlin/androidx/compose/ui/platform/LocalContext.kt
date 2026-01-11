package androidx.compose.ui.platform

import androidx.compose.runtime.staticCompositionLocalOf
import android.content.Context

private val defaultContext = Context()

val LocalContext = staticCompositionLocalOf { defaultContext }
