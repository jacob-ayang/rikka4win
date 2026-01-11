package me.rerere.rikkahub.utils

import androidx.compose.ui.graphics.Color

fun Color.toCssHex(): String {
    val r = (red * 255).toInt().coerceIn(0, 255)
    val g = (green * 255).toInt().coerceIn(0, 255)
    val b = (blue * 255).toInt().coerceIn(0, 255)
    val a = (alpha * 255).toInt().coerceIn(0, 255)
    return if (a == 255) {
        "#%02x%02x%02x".format(r, g, b)
    } else {
        "rgba(%d, %d, %d, %.3f)".format(r, g, b, alpha.coerceIn(0f, 1f))
    }
}
