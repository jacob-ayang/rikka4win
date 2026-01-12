package androidx.core.app

import android.app.Notification
import android.content.Context

class NotificationManagerCompat private constructor() {
    fun notify(id: Int, notification: Notification) = Unit

    companion object {
        fun from(context: Context): NotificationManagerCompat = NotificationManagerCompat()
    }
}
