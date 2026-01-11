package android.speech.tts

open class UtteranceProgressListener {
    open fun onStart(utteranceId: String) = Unit
    open fun onDone(utteranceId: String) = Unit
    open fun onError(utteranceId: String) = Unit
}
