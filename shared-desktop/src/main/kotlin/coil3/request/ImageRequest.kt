package coil3.request

import android.content.Context

class ImageRequest private constructor(
    val data: Any?
) {
    class Builder(@Suppress("UNUSED_PARAMETER") context: Context) {
        private var data: Any? = null

        fun data(data: Any?) = apply { this.data = data }

        fun build(): ImageRequest = ImageRequest(data)
    }
}

fun ImageRequest.Builder.placeholder(@Suppress("UNUSED_PARAMETER") resId: Int) = apply {}

fun ImageRequest.Builder.crossfade(@Suppress("UNUSED_PARAMETER") enabled: Boolean) = apply {}

fun ImageRequest.Builder.allowHardware(@Suppress("UNUSED_PARAMETER") enabled: Boolean) = apply {}
