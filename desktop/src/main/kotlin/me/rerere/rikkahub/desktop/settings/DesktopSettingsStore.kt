package me.rerere.rikkahub.desktop.settings

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.rerere.rikkahub.desktop.backup.DesktopPaths

class DesktopSettingsStore(
    private val paths: DesktopPaths,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    var settings: DesktopSettings = DesktopSettings()
        private set

    fun load(): DesktopSettings {
        val file = paths.settingsFile
        settings = if (file.exists()) {
            runCatching {
                json.decodeFromString<DesktopSettings>(file.readText())
            }.getOrElse { DesktopSettings() }
        } else {
            DesktopSettings()
        }
        return settings
    }

    fun save(value: DesktopSettings = settings) {
        if (!paths.dataDir.exists()) {
            paths.dataDir.mkdirs()
        }
        paths.settingsFile.writeText(json.encodeToString(value))
    }

    fun update(block: (DesktopSettings) -> DesktopSettings) {
        settings = block(settings)
        save(settings)
    }
}
