package me.rerere.rikkahub.utils

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

fun TextUnit.toDp(): Dp {
    return value.dp
}
