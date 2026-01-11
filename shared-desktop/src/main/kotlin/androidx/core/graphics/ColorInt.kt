package androidx.core.graphics

fun String.toColorInt(): Int {
    val value = trim().removePrefix("#")
    return value.toLongOrNull(16)?.toInt() ?: 0
}
