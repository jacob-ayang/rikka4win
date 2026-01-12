package android.content

import android.net.Uri

class Intent(
    var action: String? = null,
    var data: Uri? = null,
) {
    var type: String? = null
    private val extras = mutableMapOf<String, Any?>()
    var flags: Int = 0

    constructor(context: Context, clazz: Class<*>) : this(action = null, data = null)

    fun putExtra(name: String, value: String?): Intent {
        extras[name] = value
        return this
    }

    fun addFlags(flags: Int): Intent {
        this.flags = this.flags or flags
        return this
    }

    fun getStringExtra(name: String): String? = extras[name] as? String

    fun getCharSequenceExtra(name: String): CharSequence? = extras[name] as? CharSequence

    companion object {
        const val ACTION_SEND = "android.intent.action.SEND"
        const val ACTION_PROCESS_TEXT = "android.intent.action.PROCESS_TEXT"
        const val ACTION_VIEW = "android.intent.action.VIEW"
        const val EXTRA_TEXT = "android.intent.extra.TEXT"
        const val EXTRA_STREAM = "android.intent.extra.STREAM"
        const val EXTRA_PROCESS_TEXT = "android.intent.extra.PROCESS_TEXT"
        const val FLAG_GRANT_READ_URI_PERMISSION = 0x00000001
        const val FLAG_ACTIVITY_CLEAR_TOP = 0x04000000
        const val FLAG_ACTIVITY_SINGLE_TOP = 0x20000000

        fun createChooser(intent: Intent, title: CharSequence?): Intent = intent
    }
}
