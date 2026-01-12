package android.util

object Log {
    fun d(tag: String, msg: String, tr: Throwable? = null): Int {
        println("D/$tag: $msg")
        tr?.printStackTrace()
        return 0
    }

    fun i(tag: String, msg: String, tr: Throwable? = null): Int {
        println("I/$tag: $msg")
        tr?.printStackTrace()
        return 0
    }

    fun w(tag: String, msg: String, tr: Throwable? = null): Int {
        println("W/$tag: $msg")
        tr?.printStackTrace()
        return 0
    }

    fun e(tag: String, msg: String, tr: Throwable? = null): Int {
        System.err.println("E/$tag: $msg")
        tr?.printStackTrace()
        return 0
    }
}
