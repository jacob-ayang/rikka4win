package me.rerere.ai.util

import me.rerere.ai.ui.UIMessagePart
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

private val supportedTypes = setOf(
    "image/jpeg",
    "image/png",
    "image/gif",
    "image/webp",
)

fun UIMessagePart.Image.encodeBase64(withPrefix: Boolean = true): Result<String> = runCatching {
    when {
        url.startsWith("file://") -> {
            val file = fileFromUri(url)
            if (!file.exists()) {
                throw IllegalArgumentException("File does not exist: $url")
            }
            val mimeType = file.guessMimeType().getOrThrow()
            val (encoded, outputMime) = if (mimeType in supportedTypes) {
                file.encodeToBase64Streaming() to mimeType
            } else {
                file.convertAndEncodeToJpeg() to "image/jpeg"
            }
            if (withPrefix) "data:$outputMime;base64,$encoded" else encoded
        }

        url.startsWith("data:") -> url
        url.startsWith("http") -> url
        else -> throw IllegalArgumentException("Unsupported URL format: $url")
    }
}

fun UIMessagePart.Video.encodeBase64(withPrefix: Boolean = true): Result<String> = runCatching {
    when {
        url.startsWith("file://") -> {
            val file = fileFromUri(url)
            if (!file.exists()) {
                throw IllegalArgumentException("File does not exist: $url")
            }
            val encoded = file.encodeToBase64Streaming()
            if (withPrefix) "data:video/mp4;base64,$encoded" else encoded
        }

        else -> throw IllegalArgumentException("Unsupported URL format: $url")
    }
}

fun UIMessagePart.Audio.encodeBase64(withPrefix: Boolean = true): Result<String> = runCatching {
    when {
        url.startsWith("file://") -> {
            val file = fileFromUri(url)
            if (!file.exists()) {
                throw IllegalArgumentException("File does not exist: $url")
            }
            val encoded = file.encodeToBase64Streaming()
            if (withPrefix) "data:audio/mp3;base64,$encoded" else encoded
        }

        else -> throw IllegalArgumentException("Unsupported URL format: $url")
    }
}

private fun fileFromUri(rawUrl: String): File {
    val uri = URI(rawUrl)
    return File(uri)
}

private fun File.encodeToBase64Streaming(): String {
    val outputStream = ByteArrayOutputStream()
    Base64.getEncoder().wrap(outputStream).use { base64Stream ->
        inputStream().use { input ->
            input.copyTo(base64Stream, bufferSize = 8 * 1024)
        }
    }
    return outputStream.toString(StandardCharsets.ISO_8859_1.name())
}

private fun File.convertAndEncodeToJpeg(maxDimension: Int = 2048, quality: Int = 85): String {
    val original = ImageIO.read(this) ?: throw IllegalArgumentException("Failed to decode image: $absolutePath")
    val scaled = original.scaleToMax(maxDimension)
    val outputStream = ByteArrayOutputStream()
    val writer = ImageIO.getImageWritersByFormatName("jpg").asSequence().firstOrNull()
        ?: throw IllegalStateException("No JPEG writer available")
    val imageOutput = ImageIO.createImageOutputStream(outputStream)
    writer.output = imageOutput
    val writeParam = writer.defaultWriteParam
    if (writeParam.canWriteCompressed()) {
        writeParam.compressionMode = ImageWriteParam.MODE_EXPLICIT
        writeParam.compressionQuality = quality / 100f
    }
    writer.write(null, IIOImage(scaled, null, null), writeParam)
    imageOutput.close()
    writer.dispose()
    return Base64.getEncoder().encodeToString(outputStream.toByteArray())
}

private fun BufferedImage.scaleToMax(maxDimension: Int): BufferedImage {
    val maxSide = maxOf(width, height)
    if (maxSide <= maxDimension) return toRgbImage()
    val scale = maxDimension.toDouble() / maxSide.toDouble()
    val targetWidth = (width * scale).toInt().coerceAtLeast(1)
    val targetHeight = (height * scale).toInt().coerceAtLeast(1)
    val resized = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
    val graphics = resized.createGraphics()
    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    graphics.drawImage(this, 0, 0, targetWidth, targetHeight, null)
    graphics.dispose()
    return resized
}

private fun BufferedImage.toRgbImage(): BufferedImage {
    if (type == BufferedImage.TYPE_INT_RGB) return this
    val converted = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val graphics: Graphics2D = converted.createGraphics()
    graphics.drawImage(this, 0, 0, null)
    graphics.dispose()
    return converted
}

private fun File.guessMimeType(): Result<String> = runCatching {
    inputStream().use { input ->
        val bytes = ByteArray(16)
        val read = input.read(bytes)
        if (read < 12) error("File too short to determine MIME type")

        if (bytes.copyOfRange(4, 12).toString(Charsets.US_ASCII) == "ftypheic") {
            return@runCatching "image/heic"
        }
        if (bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()) {
            return@runCatching "image/jpeg"
        }
        if (bytes.copyOfRange(0, 8).contentEquals(
                byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
            )
        ) {
            return@runCatching "image/png"
        }
        if (bytes.copyOfRange(0, 4).toString(Charsets.US_ASCII) == "RIFF" &&
            bytes.copyOfRange(8, 12).toString(Charsets.US_ASCII) == "WEBP"
        ) {
            return@runCatching "image/webp"
        }
        val header = bytes.copyOfRange(0, 6).toString(Charsets.US_ASCII)
        if (header == "GIF89a" || header == "GIF87a") {
            return@runCatching "image/gif"
        }
        error(
            "Failed to guess MIME type: $header, ${
                bytes.joinToString(",") { it.toUByte().toString() }
            }"
        )
    }
}
