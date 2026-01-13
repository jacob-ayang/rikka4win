package me.rerere.rikkahub.utils

import android.content.Context
import android.net.Uri
import com.drew.imaging.ImageMetadataReader
import com.drew.imaging.png.PngChunkType
import com.drew.metadata.png.PngDirectory
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.io.File
import javax.imageio.ImageIO

object ImageUtils {
    fun decodeQRCodeFromUri(@Suppress("UNUSED_PARAMETER") context: Context, uri: Uri): String? {
        return runCatching {
            val file = File(uri.toString().removePrefix("file:"))
            val image = ImageIO.read(file) ?: return null
            val width = image.width
            val height = image.height
            val pixels = IntArray(width * height)
            image.getRGB(0, 0, width, height, pixels, 0, width)
            val source = RGBLuminanceSource(width, height, pixels)
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            MultiFormatReader().decode(bitmap).text
        }.getOrNull()
    }

    fun getTavernCharacterMeta(@Suppress("UNUSED_PARAMETER") context: Context, uri: Uri): Result<String> = runCatching {
        val file = File(uri.toString().removePrefix("file:"))
        val metadata = ImageMetadataReader.readMetadata(file)
        if (!metadata.containsDirectoryOfType(PngDirectory::class.java)) {
            error("No PNG directory found, please check if the image is a character card")
        }

        val pngDirectory = metadata.getDirectoriesOfType(PngDirectory::class.java)
            .firstOrNull { directory ->
                directory.pngChunkType == PngChunkType.tEXt
                    && directory.getString(PngDirectory.TAG_TEXTUAL_DATA).startsWith("[chara:")
            } ?: error("No tEXt chunk found, please check if the image is a character card")

        val value = pngDirectory.getString(PngDirectory.TAG_TEXTUAL_DATA)
        val regex = Regex("""\[chara:\s*(.+?)]""")
        regex.find(value)?.groupValues?.get(1) ?: error("No character data found")
    }
}
