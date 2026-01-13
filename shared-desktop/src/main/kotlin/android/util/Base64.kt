package android.util

import java.util.Base64 as JBase64

object Base64 {
    const val DEFAULT = 0

    fun decode(data: String, flags: Int = DEFAULT): ByteArray {
        return JBase64.getDecoder().decode(data)
    }

    fun encodeToString(input: ByteArray, flags: Int = DEFAULT): String {
        return JBase64.getEncoder().encodeToString(input)
    }
}
