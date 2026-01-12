package android.app

import android.content.Context
import android.content.Intent

class PendingIntent private constructor() {
    companion object {
        const val FLAG_UPDATE_CURRENT = 1
        const val FLAG_IMMUTABLE = 1 shl 1

        fun getActivity(
            context: Context,
            requestCode: Int,
            intent: Intent,
            flags: Int,
        ): PendingIntent = PendingIntent()
    }
}
