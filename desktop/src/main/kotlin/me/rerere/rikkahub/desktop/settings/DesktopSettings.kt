package me.rerere.rikkahub.desktop.settings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
sealed class Avatar {
    @Serializable
    @SerialName("Dummy")
    data object Dummy : Avatar()

    @Serializable
    @SerialName("Emoji")
    data class Emoji(val content: String) : Avatar()

    @Serializable
    @SerialName("Image")
    data class Image(val url: String) : Avatar()
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

@Serializable
data class S3Config(
    val endpoint: String = "",
    val accessKeyId: String = "",
    val secretAccessKey: String = "",
    val bucket: String = "",
    val region: String = "auto",
    val pathStyle: Boolean = true,
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

@Serializable
data class DesktopSettings(
    @Transient
    val init: Boolean = false,
    val dynamicColor: Boolean = true,
    val themeId: String = "sakura",
    val developerMode: Boolean = false,
    val displaySetting: DisplaySetting = DisplaySetting(),
    val enableWebSearch: Boolean = false,
    val favoriteModels: List<String> = emptyList(),
    val chatModelId: String = "",
    val titleModelId: String = "",
    val imageGenerationModelId: String = "",
    val titlePrompt: String = DefaultPrompts.title,
    val translateModeId: String = "",
    val translatePrompt: String = DefaultPrompts.translation,
    val suggestionModelId: String = "",
    val suggestionPrompt: String = DefaultPrompts.suggestion,
    val ocrModelId: String = "",
    val ocrPrompt: String = DefaultPrompts.ocr,
    val assistantId: String = "",
    val providers: JsonElement = JsonArray(emptyList()),
    val assistants: JsonElement = JsonArray(emptyList()),
    val assistantTags: JsonElement = JsonArray(emptyList()),
    val searchServices: JsonElement = JsonArray(emptyList()),
    val searchCommonOptions: JsonElement = JsonObject(emptyMap()),
    val searchServiceSelected: Int = 0,
    val mcpServers: JsonElement = JsonArray(emptyList()),
    val webDavConfig: WebDavConfig = WebDavConfig(),
    val s3Config: S3Config = S3Config(),
    val ttsProviders: JsonElement = JsonArray(emptyList()),
    val selectedTTSProviderId: String = "",
    val modeInjections: JsonElement = JsonArray(emptyList()),
    val lorebooks: JsonElement = JsonArray(emptyList()),
)

object DefaultPrompts {
    val title = """
    I will give you some dialogue content in the `<content>` block.
    You need to summarize the conversation between user and assistant into a short title.
    1. The title language should be consistent with the user's primary language
    2. Do not use punctuation or other special symbols
    3. Reply directly with the title
    4. Summarize using {locale} language
    5. The title should not exceed 10 characters

    <content>
    {content}
    </content>
""".trimIndent()

    val translation = """
    You are a translation expert, skilled in translating various languages, and maintaining accuracy, faithfulness, and elegance in translation.
    Next, I will send you text. Please translate it into {target_lang}, and return the translation result directly, without adding any explanations or other content.

    Please translate the <source_text> section:

    <source_text>
    {source_text}
    </source_text>
""".trimIndent()

    val suggestion = """
    I will provide you with some chat content in the `<content>` block, including conversations between the User and the AI assistant.
    You need to act as the **User** to reply to the assistant, generating 3~5 appropriate and contextually relevant responses to the assistant.

    Rules:
    1. Reply directly with suggestions, do not add any formatting, and separate suggestions with newlines, no need to add markdown list formats.
    2. Use {locale} language.
    3. Ensure each suggestion is valid.
    4. Each suggestion should not exceed 10 characters.
    5. Imitate the user's previous conversational style.
    6. Act as a User, not an Assistant!

    <content>
    {content}
    </content>
""".trimIndent()

    val ocr = """
    You are an OCR assistant.

    Extract all visible text from the image and also describe any non-text elements (icons, shapes, arrows, objects, symbols, or emojis).

    For each element, specify:
    - The exact text (for text) or a short description (for non-text).
    - For document-type content, please use markdown and latex format.
    - If there are objects like buildings or characters, try to identify who they are.
    - Its approximate position in the image (e.g., 'top left', 'center right', 'bottom middle').
    - Its spatial relationship to nearby elements (e.g., 'above', 'below', 'next to', 'on the left of').

    Keep the original reading order and layout structure as much as possible.
    Do not interpret or translate—only transcribe and describe what is visually present.
    """.trimIndent()
}
