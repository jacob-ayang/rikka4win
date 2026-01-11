package me.rerere.rikkahub.ui.components.webview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun WebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize())
}

sealed class WebContent {
    data class Url(
        val url: String,
        val additionalHttpHeaders: Map<String, String> = emptyMap(),
    ) : WebContent()

    data class Data(
        val data: String,
        val baseUrl: String? = null,
        val encoding: String = "utf-8",
        val mimeType: String? = null,
        val historyUrl: String? = null,
    ) : WebContent()

    data object NavigatorOnly : WebContent()
}

@Stable
class WebViewState(
    initialContent: WebContent = WebContent.NavigatorOnly,
) {
    var content: WebContent by mutableStateOf(initialContent)
    var isLoading: Boolean by mutableStateOf(false)
    var loadingProgress: Float by mutableStateOf(0f)
    var pageTitle: String? by mutableStateOf(null)
    var currentUrl: String? by mutableStateOf(null)
    var canGoBack: Boolean by mutableStateOf(false)
    var canGoForward: Boolean by mutableStateOf(false)
    var consoleMessages: List<ConsoleMessage> by mutableStateOf(emptyList())

    fun loadUrl(url: String) {
        content = WebContent.Url(url)
        currentUrl = url
    }

    fun goBack() = Unit
    fun goForward() = Unit
    fun reload() = Unit
}

@Composable
fun rememberWebViewState(
    url: String? = null,
    data: String? = null,
): WebViewState {
    return remember(url, data) {
        when {
            !url.isNullOrEmpty() -> WebViewState(WebContent.Url(url))
            !data.isNullOrEmpty() -> WebViewState(WebContent.Data(data))
            else -> WebViewState()
        }
    }
}

class ConsoleMessage(
    private val level: MessageLevel,
    private val message: String,
    private val sourceId: String = "",
    private val lineNumber: Int = 0,
) {
    enum class MessageLevel {
        TIP,
        LOG,
        WARNING,
        ERROR,
    }

    fun messageLevel(): MessageLevel = level
    fun message(): String = message
    fun sourceId(): String = sourceId
    fun lineNumber(): Int = lineNumber
}
