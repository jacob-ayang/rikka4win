package me.rerere.rikkahub.utils

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import java.io.File
import kotlin.uuid.Uuid

fun Context.createChatFilesByContents(uris: List<Uri>): List<Uri> {
    val dir = this.filesDir.resolve("upload").apply { mkdirs() }
    return uris.mapNotNull { uri ->
        runCatching {
            val file = dir.resolve(Uuid.random().toString())
            File(uri.toString().removePrefix("file:")).copyTo(file, overwrite = true)
            file.toUri()
        }.getOrNull()
    }
}

fun Context.createChatFilesByByteArrays(byteArrays: List<ByteArray>): List<Uri> {
    val dir = this.filesDir.resolve("upload").apply { mkdirs() }
    return byteArrays.mapNotNull { bytes ->
        runCatching {
            val file = dir.resolve(Uuid.random().toString())
            file.writeBytes(bytes)
            file.toUri()
        }.getOrNull()
    }
}

fun Context.deleteChatFiles(uris: List<Uri>) {
    uris.forEach { uri ->
        runCatching {
            val file = File(uri.toString().removePrefix("file:"))
            if (file.exists()) file.delete()
        }
    }
}

fun Context.getFileNameFromUri(uri: Uri): String? {
    return File(uri.toString().removePrefix("file:")).name.ifEmpty { null }
}

fun Context.getFileMimeType(uri: Uri): String? {
    return when (File(uri.toString().removePrefix("file:")).extension.lowercase()) {
        "png" -> "image/png"
        "jpg", "jpeg" -> "image/jpeg"
        "webp" -> "image/webp"
        "gif" -> "image/gif"
        "json" -> "application/json"
        "pdf" -> "application/pdf"
        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        else -> null
    }
}

fun Context.getImagesDir(): File {
    val dir = this.filesDir.resolve("images")
    if (!dir.exists()) dir.mkdirs()
    return dir
}

fun Context.createImageFileFromBase64(base64Data: String, filePath: String): File {
    val data = if (base64Data.startsWith("data:image")) {
        base64Data.substringAfter("base64,")
    } else {
        base64Data
    }
    val bytes = java.util.Base64.getDecoder().decode(data)
    val file = File(filePath)
    file.parentFile?.mkdirs()
    file.writeBytes(bytes)
    return file
}

suspend fun Context.saveMessageImage(image: String) = Unit

suspend fun Context.countChatFiles(): Pair<Int, Long> {
    val dir = filesDir.resolve("upload")
    if (!dir.exists()) return Pair(0, 0)
    val files = dir.listFiles() ?: return Pair(0, 0)
    val count = files.size
    val size = files.sumOf { it.length() }
    return Pair(count, size)
}
