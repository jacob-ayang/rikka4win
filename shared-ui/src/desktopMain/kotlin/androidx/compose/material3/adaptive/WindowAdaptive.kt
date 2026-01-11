package androidx.compose.material3.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@Composable
fun currentWindowDpSize(): DpSize {
    return DpSize(1024.dp, 768.dp)
}
