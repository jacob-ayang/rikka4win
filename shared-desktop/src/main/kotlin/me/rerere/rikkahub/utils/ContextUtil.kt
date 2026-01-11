package me.rerere.rikkahub.utils

import android.app.Activity
import android.content.Context
import me.rerere.ai.ui.UIMessage

fun Context.getActivity(): Activity? = null

fun Context.openUrl(url: String) {
    // No-op for desktop shim.
}

fun Context.copyMessageToClipboard(message: UIMessage) {
    // No-op for desktop shim.
}

fun Context.joinQQGroup(key: String?): Boolean {
    return false
}
