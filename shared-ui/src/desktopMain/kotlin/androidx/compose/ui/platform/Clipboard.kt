package androidx.compose.ui.platform

import android.content.ClipData
import androidx.compose.runtime.staticCompositionLocalOf

class ClipEntry(val clipData: ClipData)

interface Clipboard {
    suspend fun setClipEntry(entry: ClipEntry?)
    suspend fun getClipEntry(): ClipEntry?
}

private class InMemoryClipboard : Clipboard {
    private var entry: ClipEntry? = null

    override suspend fun setClipEntry(entry: ClipEntry?) {
        this.entry = entry
    }

    override suspend fun getClipEntry(): ClipEntry? = entry
}

val LocalClipboard = staticCompositionLocalOf<Clipboard> { InMemoryClipboard() }
