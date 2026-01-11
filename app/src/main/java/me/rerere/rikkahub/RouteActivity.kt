package me.rerere.rikkahub

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import me.rerere.rikkahub.ui.hooks.rememberAmoledDarkMode
import me.rerere.rikkahub.ui.hooks.rememberColorMode
import me.rerere.rikkahub.ui.hooks.rememberUserSettingsState
import me.rerere.rikkahub.ui.theme.RikkahubTheme
import me.rerere.rikkahub.ui.theme.ThemeSettings
import me.rerere.rikkahub.sharedui.AppRoutes
import me.rerere.rikkahub.sharedui.Screen
import okhttp3.OkHttpClient
import org.koin.android.ext.android.inject
import kotlin.uuid.Uuid

class RouteActivity : ComponentActivity() {
    private val okHttpClient by inject<OkHttpClient>()
    private var navStack by mutableStateOf<NavHostController?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        disableNavigationBarContrast()
        super.onCreate(savedInstanceState)
        setContent {
            val navStack = rememberNavController()
            this.navStack = navStack
            ShareHandler(navStack)
            val userSettings by rememberUserSettingsState()
            val colorMode by rememberColorMode()
            val amoledDarkMode by rememberAmoledDarkMode()
            val themeSettings = ThemeSettings(
                themeId = userSettings.themeId,
                dynamicColor = userSettings.dynamicColor,
                amoledDarkMode = amoledDarkMode,
                colorMode = colorMode,
            )
            RikkahubTheme(themeSettings) {
                setSingletonImageLoaderFactory { context ->
                    ImageLoader.Builder(context)
                        .crossfade(true)
                        .components {
                            add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient }))
                            add(SvgDecoder.Factory(scaleToDensity = true))
                        }
                        .build()
                }
                AppRoutes(navStack)
            }
        }
    }

    private fun disableNavigationBarContrast() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }

    @Composable
    private fun ShareHandler(navBackStack: NavHostController) {
        val shareIntent = remember {
            Intent().apply {
                action = intent?.action
                putExtra(Intent.EXTRA_TEXT, intent?.getStringExtra(Intent.EXTRA_TEXT))
                putExtra(Intent.EXTRA_STREAM, intent?.getStringExtra(Intent.EXTRA_STREAM))
                putExtra(Intent.EXTRA_PROCESS_TEXT, intent?.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT))
            }
        }

        LaunchedEffect(navBackStack) {
            when (shareIntent.action) {
                Intent.ACTION_SEND -> {
                    val text = shareIntent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
                    val imageUri = shareIntent.getStringExtra(Intent.EXTRA_STREAM)
                    navBackStack.navigate(Screen.ShareHandler(text, imageUri))
                }

                Intent.ACTION_PROCESS_TEXT -> {
                    val text = shareIntent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString() ?: ""
                    navBackStack.navigate(Screen.ShareHandler(text, null))
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Navigate to the chat screen if a conversation ID is provided
        intent.getStringExtra("conversationId")?.let { text ->
            navStack?.navigate(Screen.Chat(text))
        }
    }

}
