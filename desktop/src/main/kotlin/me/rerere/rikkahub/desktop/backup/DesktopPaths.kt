package me.rerere.rikkahub.desktop.backup

import java.io.File

class DesktopPaths(
    val dataDir: File = File(System.getProperty("user.home"), ".rikkahub"),
) {
    val cacheDir: File = File(dataDir, "cache")
    val settingsFile: File = File(dataDir, "settings.json")
    val dbDir: File = File(dataDir, "db")
    val dbFile: File = File(dbDir, "rikka_hub")
    val dbWalFile: File = File(dbDir, "rikka_hub-wal")
    val dbShmFile: File = File(dbDir, "rikka_hub-shm")
    val uploadDir: File = File(dataDir, "upload")

    fun ensureDirs() {
        if (!cacheDir.exists()) cacheDir.mkdirs()
        if (!dbDir.exists()) dbDir.mkdirs()
        if (!uploadDir.exists()) uploadDir.mkdirs()
    }

    fun readSettingsJson(): String {
        return if (settingsFile.exists()) {
            settingsFile.readText()
        } else {
            "{}"
        }
    }

    fun writeSettingsJson(content: String) {
        if (!dataDir.exists()) dataDir.mkdirs()
        settingsFile.writeText(content)
    }
}
