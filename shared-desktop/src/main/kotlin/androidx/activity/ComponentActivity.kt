package androidx.activity

import android.app.Application
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract

open class ComponentActivity : Application() {
    fun <I, O> registerForActivityResult(
        contract: ActivityResultContract<I, O>,
        callback: (O) -> Unit
    ): ActivityResultLauncher<I> {
        return ActivityResultLauncher()
    }
}
