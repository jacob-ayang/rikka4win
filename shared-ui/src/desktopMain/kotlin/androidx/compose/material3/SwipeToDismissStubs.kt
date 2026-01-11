package androidx.compose.material3

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val SwipeToDismissBoxDefaults.positionalThreshold: (Float) -> Float
    @Composable get() = { distance -> distance / 2f }
