package androidx.lifecycle

fun interface LifecycleEventObserver {
    fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event)
}
