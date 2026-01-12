package me.rerere.rikkahub.utils

import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.abs

@OptIn(ExperimentalEncodingApi::class)
fun String.base64Encode(): String {
    return Base64.encode(toByteArray())
}

@OptIn(ExperimentalEncodingApi::class)
fun String.base64Decode(): String {
    return runCatching { String(Base64.decode(this)) }.getOrDefault(this)
}

fun String.urlDecode(): String {
    return runCatching { URLDecoder.decode(this, StandardCharsets.UTF_8) }.getOrDefault(this)
}

fun Int.formatNumber(): String {
    val absValue = abs(this)
    val sign = if (this < 0) "-" else ""

    return when {
        absValue < 1000 -> toString()
        absValue < 1_000_000 -> {
            val value = absValue / 1000.0
            if (value == value.toInt().toDouble()) {
                "$sign${value.toInt()}K"
            } else {
                "$sign${value.toFixed(1)}K"
            }
        }
        absValue < 1_000_000_000 -> {
            val value = absValue / 1_000_000.0
            if (value == value.toInt().toDouble()) {
                "$sign${value.toInt()}M"
            } else {
                "$sign${value.toFixed(1)}M"
            }
        }
        else -> {
            val value = absValue / 1_000_000_000.0
            if (value == value.toInt().toDouble()) {
                "$sign${value.toInt()}B"
            } else {
                "$sign${value.toFixed(1)}B"
            }
        }
    }
}

fun Long.fileSizeToString(): String {
    return when {
        this < 1024 -> "$this B"
        this < 1024 * 1024 -> "${this / 1024} KB"
        this < 1024 * 1024 * 1024 -> "${this / (1024 * 1024)} MB"
        else -> "${this / (1024 * 1024 * 1024)} GB"
    }
}

fun Number.toFixed(digits: Int = 0) = "%.${digits}f".format(this)
