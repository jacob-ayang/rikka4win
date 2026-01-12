package me.rerere.rikkahub.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.rerere.ai.ui.UIMessage
import me.rerere.ai.ui.UIMessagePart
import java.util.Base64

suspend fun Context.convertBase64ImagePartToLocalFile(message: UIMessage): UIMessage =
    withContext(Dispatchers.IO) {
        message.copy(
            parts = message.parts.map { part ->
                if (part is UIMessagePart.Image && part.url.startsWith("data:image")) {
                    val encoded = part.url.substringAfter("base64,", missingDelimiterValue = "")
                    if (encoded.isBlank()) {
                        part
                    } else {
                        val bytes = Base64.getDecoder().decode(encoded)
                        val urls = createChatFilesByByteArrays(listOf(bytes))
                        part.copy(url = urls.firstOrNull()?.toString() ?: part.url)
                    }
                } else {
                    part
                }
            }
        )
    }
