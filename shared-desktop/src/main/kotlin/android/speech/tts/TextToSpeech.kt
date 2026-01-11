package android.speech.tts

import android.content.Context

class TextToSpeech(
    context: Context,
    listener: OnInitListener
) {
    init {
        listener.onInit(SUCCESS)
    }

    fun setLanguage(locale: java.util.Locale): Int = LANG_AVAILABLE

    fun setOnUtteranceProgressListener(listener: UtteranceProgressListener) = Unit

    fun synthesizeToFile(
        text: String,
        params: HashMap<String, String>,
        file: java.io.File,
        utteranceId: String
    ): Int = SUCCESS

    fun shutdown() = Unit

    companion object {
        const val SUCCESS = 0
        const val LANG_MISSING_DATA = -1
        const val LANG_NOT_SUPPORTED = -2
        const val LANG_AVAILABLE = 1
    }

    fun interface OnInitListener {
        fun onInit(status: Int)
    }
}
