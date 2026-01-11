package android.widget

import android.content.Context

class Toast private constructor(private val text: String) {
    fun show() {
        println("Toast: $text")
    }

    companion object {
        const val LENGTH_SHORT = 0
        const val LENGTH_LONG = 1

        fun makeText(context: Context, text: CharSequence, duration: Int): Toast {
            return Toast(text.toString())
        }
    }
}
