package androidx.core.app

import android.app.Notification
import android.app.PendingIntent
import android.content.Context

object NotificationCompat {
    const val VISIBILITY_PRIVATE = 0
    const val DEFAULT_ALL = 0
    const val CATEGORY_MESSAGE = "message"

    class Builder(private val context: Context, private val channelId: String) {
        fun setContentTitle(title: CharSequence?): Builder = this
        fun setContentText(text: CharSequence?): Builder = this
        fun setSmallIcon(icon: Int): Builder = this
        fun setVisibility(visibility: Int): Builder = this
        fun setAutoCancel(autoCancel: Boolean): Builder = this
        fun setDefaults(defaults: Int): Builder = this
        fun setCategory(category: String): Builder = this
        fun setContentIntent(intent: PendingIntent?): Builder = this
        fun build(): Notification = Notification()
    }
}
