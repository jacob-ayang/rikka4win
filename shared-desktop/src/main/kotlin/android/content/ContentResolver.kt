package android.content

import android.net.Uri
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class ContentResolver {
    fun openInputStream(uri: Uri): InputStream? {
        return runCatching {
            FileInputStream(uri.toString().removePrefix("file:"))
        }.getOrNull()
    }

    fun openOutputStream(uri: Uri): OutputStream? {
        return runCatching {
            FileOutputStream(uri.toString().removePrefix("file:"))
        }.getOrNull()
    }

    fun getType(uri: Uri): String? = null
}
