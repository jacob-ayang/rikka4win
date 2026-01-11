package io.github.g00fy2.quickie

import androidx.activity.result.contract.ActivityResultContract

sealed class QRResult {
    data class QRSuccess(val content: QRContent) : QRResult()
    data class QRError(val exception: Throwable? = null) : QRResult()
    data object QRUserCanceled : QRResult()
    data object QRMissingPermission : QRResult()
}

data class QRContent(val rawValue: String?)

class ScanQRCode : ActivityResultContract<Any?, QRResult>() {
    override fun createIntent(context: android.content.Context, input: Any?): android.content.Intent {
        return android.content.Intent()
    }

    override fun parseResult(resultCode: Int, intent: android.content.Intent?): QRResult {
        return QRResult.QRUserCanceled
    }
}
