package me.rerere.rikkahub.di

import android.app.Application
import kotlinx.serialization.json.Json
import com.google.firebase.analytics.FirebaseAnalytics
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import me.rerere.ai.provider.ProviderManager
import me.rerere.highlight.Highlighter
import me.rerere.rikkahub.data.ai.transformers.AssistantTemplateLoader
import me.rerere.rikkahub.data.ai.transformers.TemplateTransformer
import me.rerere.rikkahub.data.ai.tools.LocalTools
import me.rerere.rikkahub.AppScope
import me.rerere.rikkahub.data.ai.mcp.McpManager
import me.rerere.rikkahub.data.ai.GenerationHandler
import me.rerere.rikkahub.data.ai.AILoggingManager
import me.rerere.rikkahub.data.api.SponsorAPI
import me.rerere.rikkahub.data.db.DesktopDatabase
import me.rerere.rikkahub.data.datastore.SettingsStore
import me.rerere.rikkahub.data.repository.ConversationRepository
import me.rerere.rikkahub.data.repository.GenMediaRepository
import me.rerere.rikkahub.data.repository.MemoryRepository
import me.rerere.rikkahub.service.ChatService
import me.rerere.rikkahub.utils.JsonInstant
import me.rerere.rikkahub.utils.UpdateChecker
import me.rerere.rikkahub.data.sync.S3Sync
import me.rerere.rikkahub.data.sync.WebdavSync
import io.pebbletemplates.pebble.PebbleEngine
import okhttp3.OkHttpClient
import org.koin.dsl.module
import java.util.concurrent.TimeUnit
import java.util.Locale

val desktopCoreModule = module {
    single<Json> { JsonInstant }
    single { AppScope() }
    single { Application() }
    single { Highlighter() }
    single { SettingsStore(context = get(), scope = get()) }
    single { DesktopDatabase(context = get()) }
    single { ConversationRepository(context = get(), database = get()) }
    single { MemoryRepository(database = get()) }
    single { GenMediaRepository(database = get()) }
    single { McpManager(settingsStore = get(), appScope = get()) }
    single { UpdateChecker() }
    single { AILoggingManager() }
    single {
        GenerationHandler(
            context = get(),
            providerManager = get(),
            json = get(),
            memoryRepo = get(),
            conversationRepo = get(),
            aiLoggingManager = get(),
        )
    }
    single<OkHttpClient> {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(120, TimeUnit.SECONDS)
            .followSslRedirects(true)
            .followRedirects(true)
            .retryOnConnectionFailure(true)
            .build()
    }
    single { ProviderManager(client = get()) }
    single { FirebaseAnalytics() }
    single { SponsorAPI.create() }
    single { LocalTools() }
    single { AssistantTemplateLoader(settingsStore = get()) }
    single {
        PebbleEngine.Builder()
            .loader(get<AssistantTemplateLoader>())
            .defaultLocale(Locale.getDefault())
            .autoEscaping(false)
            .build()
    }
    single { TemplateTransformer(engine = get(), settingsStore = get()) }
    single { WebdavSync(settingsStore = get(), json = get(), context = get()) }
    single<HttpClient> {
        HttpClient(OkHttp) {
            engine {
                config {
                    connectTimeout(20, TimeUnit.SECONDS)
                    readTimeout(10, TimeUnit.MINUTES)
                    writeTimeout(120, TimeUnit.SECONDS)
                    followSslRedirects(true)
                    followRedirects(true)
                    retryOnConnectionFailure(true)
                }
            }
        }
    }
    single { S3Sync(settingsStore = get(), json = get(), context = get(), httpClient = get()) }
    single {
        ChatService(
            context = get(),
            appScope = get(),
            settingsStore = get(),
            conversationRepo = get(),
            memoryRepository = get(),
            generationHandler = get(),
            templateTransformer = get(),
            providerManager = get(),
            localTools = get(),
            mcpManager = get(),
        )
    }
}
