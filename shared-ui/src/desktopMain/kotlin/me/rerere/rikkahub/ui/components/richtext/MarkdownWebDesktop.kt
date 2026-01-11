package me.rerere.rikkahub.ui.components.richtext

import android.content.Context
import androidx.compose.material3.ColorScheme
import me.rerere.rikkahub.utils.toCssHex

fun buildMarkdownPreviewHtml(
    context: Context,
    markdown: String,
    colorScheme: ColorScheme
): String {
    val safeMarkdown = markdown
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8" />
          <style>
            body {
              margin: 0;
              padding: 16px;
              background: ${colorScheme.background.toCssHex()};
              color: ${colorScheme.onBackground.toCssHex()};
              font-family: sans-serif;
            }
            pre {
              white-space: pre-wrap;
              word-wrap: break-word;
            }
          </style>
        </head>
        <body>
          <pre>$safeMarkdown</pre>
        </body>
        </html>
    """.trimIndent()
}
