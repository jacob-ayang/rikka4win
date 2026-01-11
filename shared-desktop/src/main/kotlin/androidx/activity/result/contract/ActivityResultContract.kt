package androidx.activity.result.contract

open class ActivityResultContract<I, O> {
    open fun createIntent(context: android.content.Context, input: I): android.content.Intent {
        return android.content.Intent()
    }

    open fun parseResult(resultCode: Int, intent: android.content.Intent?): O {
        @Suppress("UNCHECKED_CAST")
        return null as O
    }
}
