package me.rerere.rikkahub.data.datastore

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import me.rerere.ai.provider.Model
import me.rerere.ai.provider.ProviderSetting
import me.rerere.rikkahub.AppScope
import me.rerere.rikkahub.data.ai.mcp.McpServerConfig
import me.rerere.rikkahub.data.ai.prompts.DEFAULT_OCR_PROMPT
import me.rerere.rikkahub.data.ai.prompts.DEFAULT_SUGGESTION_PROMPT
import me.rerere.rikkahub.data.ai.prompts.DEFAULT_TITLE_PROMPT
import me.rerere.rikkahub.data.ai.prompts.DEFAULT_TRANSLATION_PROMPT
import me.rerere.rikkahub.data.model.Assistant
import me.rerere.rikkahub.data.model.Avatar
import me.rerere.rikkahub.data.model.Lorebook
import me.rerere.rikkahub.data.model.PromptInjection
import me.rerere.rikkahub.data.model.Tag
import me.rerere.rikkahub.data.sync.s3.S3Config
import me.rerere.rikkahub.utils.JsonInstant
import me.rerere.search.SearchCommonOptions
import me.rerere.search.SearchServiceOptions
import me.rerere.tts.provider.TTSProviderSetting
import java.io.File
import kotlin.uuid.Uuid

private const val TAG = "SettingsStore"

class SettingsStore(
    private val context: Context,
    private val scope: AppScope,
) {
    private val settingsFile = File(context.filesDir, "settings.json")

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<Settings> = _settingsFlow
    val settingsFlowRaw: StateFlow<Settings> = _settingsFlow

    suspend fun update(settings: Settings) {
        if (settings.init) {
            Log.w(TAG, "Cannot update dummy settings")
            return
        }
        val normalized = settings.ensureDefaults()
        _settingsFlow.value = normalized
        persist(normalized)
    }

    suspend fun update(fn: (Settings) -> Settings) {
        update(fn(_settingsFlow.value))
    }

    suspend fun updateAssistant(assistantId: Uuid) {
        update(_settingsFlow.value.copy(assistantId = assistantId))
    }

    private fun loadSettings(): Settings {
        return runCatching {
            if (!settingsFile.exists()) return Settings.dummy()
            JsonInstant.decodeFromString(Settings.serializer(), settingsFile.readText())
        }.getOrElse { Settings.dummy() }
            .copy(init = false)
            .ensureDefaults()
    }

    private fun persist(settings: Settings) {
        runCatching {
            settingsFile.parentFile?.mkdirs()
            settingsFile.writeText(JsonInstant.encodeToString(Settings.serializer(), settings))
        }.onFailure {
            Log.e(TAG, "Failed to persist settings", it)
        }
    }
}

@Serializable
data class Settings(
    @kotlinx.serialization.Transient
    val init: Boolean = false,
    val dynamicColor: Boolean = true,
    val themeId: String = "sakura",
    val developerMode: Boolean = false,
    val displaySetting: DisplaySetting = DisplaySetting(),
    val enableWebSearch: Boolean = false,
    val favoriteModels: List<Uuid> = emptyList(),
    val chatModelId: Uuid = Uuid.random(),
    val titleModelId: Uuid = Uuid.random(),
    val imageGenerationModelId: Uuid = Uuid.random(),
    val titlePrompt: String = DEFAULT_TITLE_PROMPT,
    val translateModeId: Uuid = Uuid.random(),
    val translatePrompt: String = DEFAULT_TRANSLATION_PROMPT,
    val suggestionModelId: Uuid = Uuid.random(),
    val suggestionPrompt: String = DEFAULT_SUGGESTION_PROMPT,
    val ocrModelId: Uuid = Uuid.random(),
    val ocrPrompt: String = DEFAULT_OCR_PROMPT,
    val assistantId: Uuid = DEFAULT_ASSISTANT_ID,
    val providers: List<ProviderSetting> = DEFAULT_PROVIDERS,
    val assistants: List<Assistant> = DEFAULT_ASSISTANTS,
    val assistantTags: List<Tag> = emptyList(),
    val searchServices: List<SearchServiceOptions> = listOf(SearchServiceOptions.DEFAULT),
    val searchCommonOptions: SearchCommonOptions = SearchCommonOptions(),
    val searchServiceSelected: Int = 0,
    val mcpServers: List<McpServerConfig> = emptyList(),
    val webDavConfig: WebDavConfig = WebDavConfig(),
    val s3Config: S3Config = S3Config(),
    val ttsProviders: List<TTSProviderSetting> = DEFAULT_TTS_PROVIDERS,
    val selectedTTSProviderId: Uuid = DEFAULT_SYSTEM_TTS_ID,
    val modeInjections: List<PromptInjection.ModeInjection> = emptyList(),
    val lorebooks: List<Lorebook> = emptyList(),
) {
    companion object {
        fun dummy() = Settings(init = true)
    }
}

@Serializable
data class DisplaySetting(
    val userAvatar: Avatar = Avatar.Dummy,
    val userNickname: String = "",
    val showUserAvatar: Boolean = true,
    val showModelIcon: Boolean = true,
    val showModelName: Boolean = true,
    val showTokenUsage: Boolean = true,
    val autoCloseThinking: Boolean = true,
    val showUpdates: Boolean = true,
    val showMessageJumper: Boolean = true,
    val messageJumperOnLeft: Boolean = false,
    val fontSizeRatio: Float = 1.0f,
    val enableMessageGenerationHapticEffect: Boolean = false,
    val skipCropImage: Boolean = false,
    val enableNotificationOnMessageGeneration: Boolean = false,
    val codeBlockAutoWrap: Boolean = false,
    val codeBlockAutoCollapse: Boolean = false,
    val showLineNumbers: Boolean = false,
)

@Serializable
data class WebDavConfig(
    val url: String = "",
    val username: String = "",
    val password: String = "",
    val path: String = "rikkahub_backups",
    val items: List<BackupItem> = listOf(
        BackupItem.DATABASE,
        BackupItem.FILES
    ),
) {
    @Serializable
    enum class BackupItem {
        DATABASE,
        FILES,
    }
}

fun Settings.isNotConfigured() = providers.all { it.models.isEmpty() }

fun Settings.findModelById(uuid: Uuid): Model? {
    return this.providers.findModelById(uuid)
}

fun List<ProviderSetting>.findModelById(uuid: Uuid): Model? {
    this.forEach { setting ->
        setting.models.forEach { model ->
            if (model.id == uuid) {
                return model
            }
        }
    }
    return null
}

fun Settings.getCurrentChatModel(): Model? {
    return findModelById(this.getCurrentAssistant().chatModelId ?: this.chatModelId)
}

fun Settings.getCurrentAssistant(): Assistant {
    return this.assistants.find { it.id == assistantId } ?: this.assistants.first()
}

fun Settings.getAssistantById(id: Uuid): Assistant? {
    return this.assistants.find { it.id == id }
}

fun Settings.getSelectedTTSProvider(): TTSProviderSetting? {
    return selectedTTSProviderId?.let { id ->
        ttsProviders.find { it.id == id }
    } ?: ttsProviders.firstOrNull()
}

fun Model.findProvider(providers: List<ProviderSetting>, checkOverwrite: Boolean = true): ProviderSetting? {
    val provider = findModelProviderFromList(providers) ?: return null
    val providerOverwrite = this.providerOverwrite
    if (checkOverwrite && providerOverwrite != null) {
        return providerOverwrite.copyProvider(proxy = provider.proxy, models = emptyList())
    }
    return provider
}

private fun Model.findModelProviderFromList(providers: List<ProviderSetting>): ProviderSetting? {
    providers.forEach { setting ->
        setting.models.forEach { model ->
            if (model.id == this.id) {
                return setting
            }
        }
    }
    return null
}

val DEFAULT_ASSISTANT_ID = Uuid.parse("0950e2dc-9bd5-4801-afa3-aa887aa36b4e")
internal val DEFAULT_ASSISTANTS = listOf(
    Assistant(
        id = DEFAULT_ASSISTANT_ID,
        name = "",
        systemPrompt = ""
    ),
    Assistant(
        id = Uuid.parse("3d47790c-c415-4b90-9388-751128adb0a0"),
        name = "",
        systemPrompt = """
            You are a helpful assistant, called {{char}}, based on model {{model_name}}.

            ## Info
            - Time: {{cur_datetime}}
            - Locale: {{locale}}
            - Timezone: {{timezone}}
            - Device Info: {{device_info}}
            - System Version: {{system_version}}
            - User Nickname: {{user}}

            ## Hint
            - If the user does not specify a language, reply in the user's primary language.
            - Remember to use Markdown syntax for formatting, and use latex for mathematical expressions.
        """.trimIndent()
    ),
)

val DEFAULT_SYSTEM_TTS_ID = Uuid.parse("026a01a2-c3a0-4fd5-8075-80e03bdef200")
private val DEFAULT_TTS_PROVIDERS = listOf(
    TTSProviderSetting.SystemTTS(
        id = DEFAULT_SYSTEM_TTS_ID,
        name = "",
    ),
    TTSProviderSetting.OpenAI(
        id = Uuid.parse("e36b22ef-ca82-40ab-9e70-60cad861911c"),
        name = "AiHubMix",
        baseUrl = "https://aihubmix.com/v1",
        model = "gpt-4o-mini-tts",
        voice = "alloy",
    )
)

val DEFAULT_ASSISTANTS_IDS = DEFAULT_ASSISTANTS.map { it.id }

private fun Settings.ensureDefaults(): Settings {
    var providers = if (this.providers.isEmpty()) DEFAULT_PROVIDERS else this.providers
    DEFAULT_PROVIDERS.forEach { defaultProvider ->
        if (providers.none { it.id == defaultProvider.id }) {
            providers = providers + defaultProvider.copyProvider()
        }
    }
    val normalizedProviders = providers
        .map { provider ->
            val providerWithModels = when (provider) {
                is ProviderSetting.OpenAI -> provider.copy(models = provider.models.distinctBy { it.id })
                is ProviderSetting.Google -> provider.copy(models = provider.models.distinctBy { it.id })
                is ProviderSetting.Claude -> provider.copy(models = provider.models.distinctBy { it.id })
            }
            val defaultProvider = DEFAULT_PROVIDERS.find { it.id == provider.id }
            if (defaultProvider != null) {
                providerWithModels.copyProvider(
                    builtIn = defaultProvider.builtIn,
                    description = defaultProvider.description,
                    shortDescription = defaultProvider.shortDescription
                )
            } else {
                providerWithModels
            }
        }
        .distinctBy { it.id }

    val assistants = if (this.assistants.isEmpty()) DEFAULT_ASSISTANTS else this.assistants
    val assistantList = assistants.toMutableList()
    DEFAULT_ASSISTANTS.forEach { defaultAssistant ->
        if (assistantList.none { it.id == defaultAssistant.id }) {
            assistantList.add(defaultAssistant.copy())
        }
    }
    val validMcpServerIds = mcpServers.map { it.id }.toSet()
    val validModeInjectionIds = modeInjections.map { it.id }.toSet()
    val validLorebookIds = lorebooks.map { it.id }.toSet()
    val normalizedAssistants = assistantList
        .distinctBy { it.id }
        .map { assistant ->
            assistant.copy(
                mcpServers = assistant.mcpServers.filter { it in validMcpServerIds }.toSet(),
                modeInjectionIds = assistant.modeInjectionIds.filter { it in validModeInjectionIds }.toSet(),
                lorebookIds = assistant.lorebookIds.filter { it in validLorebookIds }.toSet(),
            )
        }

    val ttsProviders = if (this.ttsProviders.isEmpty()) DEFAULT_TTS_PROVIDERS else this.ttsProviders
    val ttsList = ttsProviders.toMutableList()
    DEFAULT_TTS_PROVIDERS.forEach { defaultTts ->
        if (ttsList.none { it.id == defaultTts.id }) {
            ttsList.add(defaultTts.copyProvider())
        }
    }
    val normalizedTts = ttsList.distinctBy { it.id }

    return copy(
        providers = normalizedProviders,
        assistants = normalizedAssistants,
        ttsProviders = normalizedTts
    )
}
