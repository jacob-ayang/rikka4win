package me.rerere.rikkahub.ui.components.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class GridAnimationMode {
    Diagonal, CenterOut, Snake, Matrix, Pulse,
    Wave, WaveVertical, Spiral, Checkerboard,
    Cross, Corners, Rows, Columns, Bounce, Ripple
}

@Composable
fun GlowGridLoading(
    modifier: Modifier = Modifier,
    mode: GridAnimationMode = GridAnimationMode.Diagonal,
) = Unit
