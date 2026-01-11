package me.rerere.rikkahub.data.sync

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.rerere.rikkahub.data.datastore.SettingsStore
import me.rerere.rikkahub.data.datastore.WebDavConfig
import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

private const val TAG = "WebdavSync"

@Serializable
data class WebDavBackupItem(
    val href: String,
    val displayName: String,
    val size: Long,
    val lastModified: Long,
)

class WebdavSync(
    private val settingsStore: SettingsStore,
    private val json: Json,
    private val context: Context,
) {
    suspend fun testWebdav(webDavConfig: WebDavConfig) {
        Log.i(TAG, "testWebdav: noop for desktop")
    }

    suspend fun listBackupFiles(webDavConfig: WebDavConfig): List<WebDavBackupItem> = withContext(Dispatchers.IO) {
        val folder = backupFolder()
        if (!folder.exists()) return@withContext emptyList()
        folder.listFiles()
            ?.filter { it.extension == "zip" }
            ?.map {
                WebDavBackupItem(
                    href = it.name,
                    displayName = it.name,
                    size = it.length(),
                    lastModified = it.lastModified()
                )
            }
            ?: emptyList()
    }

    suspend fun backupToWebDav(webDavConfig: WebDavConfig) {
        val file = prepareBackupFile(webDavConfig)
        val target = File(backupFolder(), file.name)
        file.copyTo(target, overwrite = true)
    }

    suspend fun restoreFromWebDav(webDavConfig: WebDavConfig, item: WebDavBackupItem) {
        val target = File(backupFolder(), item.href)
        if (target.exists()) {
            restoreFromLocalFile(target, webDavConfig)
        }
    }

    suspend fun deleteWebDavBackupFile(webDavConfig: WebDavConfig, item: WebDavBackupItem) {
        val target = File(backupFolder(), item.href)
        if (target.exists()) target.delete()
    }

    suspend fun prepareBackupFile(webDavConfig: WebDavConfig): File = withContext(Dispatchers.IO) {
        val timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now()).replace(":", "-")
        val backupFile = File(context.cacheDir, "rikkahub_backup_$timestamp.zip")
        val dbFile = context.getDatabasePath("rikka_hub")
        val uploadFolder = File(context.filesDir, "upload")
        val settingsFile = File(context.filesDir, "settings.json")

        ZipOutputStream(backupFile.outputStream()).use { zip ->
            if (dbFile.exists()) {
                zip.putNextEntry(ZipEntry("rikka_hub.db"))
                dbFile.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
            if (settingsFile.exists()) {
                zip.putNextEntry(ZipEntry("settings.json"))
                settingsFile.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
            if (uploadFolder.exists()) {
                uploadFolder.walkTopDown().filter { it.isFile }.forEach { file ->
                    zip.putNextEntry(ZipEntry("upload/${file.name}"))
                    file.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            }
        }
        backupFile
    }

    suspend fun restoreFromLocalFile(file: File, webDavConfig: WebDavConfig) = withContext(Dispatchers.IO) {
        ZipInputStream(file.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                when (entry.name) {
                    "rikka_hub.db" -> {
                        val dbFile = context.getDatabasePath("rikka_hub")
                        dbFile.outputStream().use { zip.copyTo(it) }
                    }
                    "settings.json" -> {
                        val settingsFile = File(context.filesDir, "settings.json")
                        settingsFile.outputStream().use { zip.copyTo(it) }
                    }
                    else -> if (entry.name.startsWith("upload/")) {
                        val uploadFolder = File(context.filesDir, "upload").apply { mkdirs() }
                        val outFile = File(uploadFolder, entry.name.removePrefix("upload/"))
                        outFile.outputStream().use { zip.copyTo(it) }
                    }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
    }

    private fun backupFolder(): File = File(context.filesDir, "backups").apply { mkdirs() }
}
