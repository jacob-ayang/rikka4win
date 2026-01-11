package android.net

import java.io.File
import java.net.URI

data class Uri(private val uri: URI) {
    companion object {
        fun parse(value: String): Uri = Uri(URI.create(value))
        fun fromFile(file: File): Uri = Uri(file.toURI())
    }

    val scheme: String?
        get() = uri.scheme

    override fun toString(): String = uri.toString()

    fun toJavaUri(): URI = uri
}
