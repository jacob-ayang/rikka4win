package androidx.activity.compose

import androidx.compose.runtime.Composable

@Composable
fun BackHandler(enabled: Boolean = true, onBack: () -> Unit) {
    // Desktop shim: no system back handling.
}
