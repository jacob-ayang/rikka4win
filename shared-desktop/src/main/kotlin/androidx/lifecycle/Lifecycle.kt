package androidx.lifecycle

class Lifecycle {
    enum class Event {
        ON_START,
        ON_STOP,
    }

    fun addObserver(observer: LifecycleEventObserver) = Unit

    fun removeObserver(observer: LifecycleEventObserver) = Unit
}

interface LifecycleOwner {
    val lifecycle: Lifecycle
}
