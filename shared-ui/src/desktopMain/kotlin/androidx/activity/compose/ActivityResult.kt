package androidx.activity.compose

import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable

class ManagedActivityResultLauncher<I, O>(
    private val onResult: (O) -> Unit
) {
    fun launch(input: I) {
        // No-op for desktop shim.
    }
}

@Composable
fun <I, O> rememberLauncherForActivityResult(
    contract: ActivityResultContract<I, O>,
    onResult: (O) -> Unit
): ManagedActivityResultLauncher<I, O> {
    return ManagedActivityResultLauncher(onResult)
}
