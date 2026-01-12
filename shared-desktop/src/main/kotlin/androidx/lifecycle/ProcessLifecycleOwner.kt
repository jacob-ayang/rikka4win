package androidx.lifecycle

class ProcessLifecycleOwner private constructor() : LifecycleOwner {
    override val lifecycle: Lifecycle = Lifecycle()

    companion object {
        private val instance = ProcessLifecycleOwner()

        fun get(): ProcessLifecycleOwner = instance
    }
}
