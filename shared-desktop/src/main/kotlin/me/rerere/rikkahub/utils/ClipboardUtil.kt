package me.rerere.rikkahub.utils

import android.content.ClipData

fun ClipData.getText(): String {
    return buildString {
        repeat(itemCount) { index ->
            append(getItemAt(index).text ?: "")
        }
    }
}
