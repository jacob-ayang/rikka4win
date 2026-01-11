package me.rerere.rikkahub.desktop

import me.rerere.rikkahub.di.desktopCoreModule
import me.rerere.rikkahub.ui.pages.assistant.AssistantVM
import me.rerere.rikkahub.ui.pages.assistant.detail.AssistantDetailVM
import me.rerere.rikkahub.ui.pages.backup.BackupVM
import me.rerere.rikkahub.ui.pages.chat.ChatVM
import me.rerere.rikkahub.ui.pages.debug.DebugVM
import me.rerere.rikkahub.ui.pages.developer.DeveloperVM
import me.rerere.rikkahub.ui.pages.history.HistoryVM
import me.rerere.rikkahub.ui.pages.imggen.ImgGenVM
import me.rerere.rikkahub.ui.pages.prompts.PromptVM
import me.rerere.rikkahub.ui.pages.setting.SettingVM
import me.rerere.rikkahub.ui.pages.share.handler.ShareHandlerVM
import me.rerere.rikkahub.ui.pages.translator.TranslatorVM
import org.koin.dsl.module

val desktopModule = module {
    includes(desktopCoreModule)

    factory { (id: String) ->
        ChatVM(
            id = id,
            context = get(),
            settingsStore = get(),
            conversationRepo = get(),
            chatService = get(),
            updateChecker = get(),
            analytics = get(),
        )
    }
    factory { SettingVM(settingsStore = get(), mcpManager = get()) }
    factory { DebugVM(settingsStore = get()) }
    factory { HistoryVM(settingsStore = get(), conversationRepo = get()) }
    factory { AssistantVM(settingsStore = get(), memoryRepository = get(), conversationRepo = get()) }
    factory { (id: String) ->
        AssistantDetailVM(
            id = id,
            settingsStore = get(),
            memoryRepository = get(),
            context = get(),
        )
    }
    factory { TranslatorVM(settingsStore = get()) }
    factory { (text: String) ->
        ShareHandlerVM(
            text = text,
            settingsStore = get(),
        )
    }
    factory { BackupVM(settingsStore = get(), webdavSync = get(), s3Sync = get()) }
    factory { ImgGenVM(context = get(), settingsStore = get(), genMediaRepository = get(), providerManager = get()) }
    factory { DeveloperVM() }
    factory { PromptVM(settingsStore = get()) }
}
