package androidx.compose.ui.util

fun Double.fastCoerceAtLeast(minimumValue: Double): Double {
    return if (this < minimumValue) minimumValue else this
}

fun Int.fastCoerceAtLeast(minimumValue: Int): Int {
    return if (this < minimumValue) minimumValue else this
}
