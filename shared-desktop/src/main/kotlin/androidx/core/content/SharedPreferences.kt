package androidx.core.content

import android.content.SharedPreferences

inline fun SharedPreferences.edit(
    commit: Boolean = false,
    action: SharedPreferences.Editor.() -> Unit
) {
    val editor = edit()
    editor.action()
    editor.apply()
}
