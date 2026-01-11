package me.rerere.rikkahub.utils

import java.util.Base64
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

fun String.base64Encode(): String {
    return Base64.getEncoder().encodeToString(toByteArray())
}

fun String.base64Decode(): String {
    return runCatching {
        String(Base64.getDecoder().decode(this))
    }.getOrDefault(this)
}

fun String.urlDecode(): String {
    return runCatching {
        URLDecoder.decode(this, StandardCharsets.UTF_8)
    }.getOrDefault(this)
}

fun Int.formatNumber(): String {
    return when {
        this >= 1_000_000 -> String.format("%.1fM", this / 1_000_000.0)
        this >= 1_000 -> String.format("%.1fK", this / 1_000.0)
        else -> toString()
    }
}

fun Long.fileSizeToString(): String {
    return when {
        this < 1024 -> "${this} B"
        this < 1024 * 1024 -> "${this / 1024} KB"
        this < 1024 * 1024 * 1024 -> "${this / (1024 * 1024)} MB"
        else -> "${this / (1024 * 1024 * 1024)} GB"
    }
}
