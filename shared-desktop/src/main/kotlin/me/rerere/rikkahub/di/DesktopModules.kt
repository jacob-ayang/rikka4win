package me.rerere.rikkahub.di

import android.app.Application
import kotlinx.serialization.json.Json
import me.rerere.ai.provider.ProviderManager
import com.google.firebase.analytics.FirebaseAnalytics
import me.rerere.highlight.Highlighter
import me.rerere.rikkahub.AppScope
import me.rerere.rikkahub.data.ai.mcp.McpManager
import me.rerere.rikkahub.data.ai.GenerationHandler
import me.rerere.rikkahub.data.ai.AILoggingManager
import me.rerere.rikkahub.data.api.SponsorAPI
import me.rerere.rikkahub.data.datastore.SettingsStore
import me.rerere.rikkahub.data.repository.ConversationRepository
import me.rerere.rikkahub.data.repository.GenMediaRepository
import me.rerere.rikkahub.data.repository.MemoryRepository
import me.rerere.rikkahub.data.sync.S3Sync
import me.rerere.rikkahub.data.sync.WebdavSync
import me.rerere.rikkahub.service.ChatService
import me.rerere.rikkahub.utils.JsonInstant
import me.rerere.rikkahub.utils.UpdateChecker
import org.koin.dsl.module

val desktopCoreModule = module {
    single<Json> { JsonInstant }
    single { AppScope() }
    single { Application() }
    single { Highlighter() }
    single { SettingsStore(context = get(), scope = get()) }
    single { ConversationRepository(context = get()) }
    single { MemoryRepository() }
    single { GenMediaRepository() }
    single { McpManager() }
    single { UpdateChecker() }
    single { GenerationHandler() }
    single { ProviderManager() }
    single { FirebaseAnalytics() }
    single { AILoggingManager() }
    single { SponsorAPI.create() }
    single { WebdavSync(settingsStore = get(), json = get(), context = get()) }
    single { S3Sync(settingsStore = get(), json = get(), context = get()) }
    single {
        ChatService(
            context = get(),
            appScope = get(),
            settingsStore = get(),
            conversationRepo = get(),
            memoryRepository = get(),
            mcpManager = get(),
        )
    }
}
