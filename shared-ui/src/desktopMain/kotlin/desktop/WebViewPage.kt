package me.rerere.rikkahub.ui.pages.webview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.rerere.rikkahub.ui.components.nav.BackButton

@Composable
fun WebViewPage(url: String, content: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "WebView") },
                navigationIcon = { BackButton() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
        ) {
            val display = when {
                url.isNotBlank() -> "URL: $url"
                content.isNotBlank() -> "Content preview is unavailable on desktop."
                else -> "No content."
            }
            Text(text = display, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
