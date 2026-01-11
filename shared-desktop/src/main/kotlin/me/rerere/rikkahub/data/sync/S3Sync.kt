package me.rerere.rikkahub.data.sync

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import me.rerere.rikkahub.data.datastore.SettingsStore
import me.rerere.rikkahub.data.sync.s3.S3Config
import java.io.File

private const val TAG = "S3Sync"

@Serializable
data class S3BackupItem(
    val key: String,
    val displayName: String,
    val size: Long,
    val lastModified: Long,
)

class S3Sync(
    private val settingsStore: SettingsStore,
    private val json: kotlinx.serialization.json.Json,
    private val context: Context,
) {
    suspend fun testS3(config: S3Config) {
        Log.i(TAG, "testS3: noop for desktop")
    }

    suspend fun listBackupFiles(config: S3Config): List<S3BackupItem> = withContext(Dispatchers.IO) {
        val folder = backupFolder()
        if (!folder.exists()) return@withContext emptyList()
        folder.listFiles()
            ?.filter { it.extension == "zip" }
            ?.map {
                S3BackupItem(
                    key = it.name,
                    displayName = it.name,
                    size = it.length(),
                    lastModified = it.lastModified()
                )
            }
            ?: emptyList()
    }

    suspend fun backupToS3(config: S3Config) = withContext(Dispatchers.IO) {
        val source = File(context.filesDir, "backups").listFiles()?.maxByOrNull { it.lastModified() }
        if (source != null) {
            val target = File(backupFolder(), source.name)
            source.copyTo(target, overwrite = true)
        }
    }

    suspend fun restoreFromS3(config: S3Config, item: S3BackupItem) = withContext(Dispatchers.IO) {
        val file = File(backupFolder(), item.key)
        if (file.exists()) {
            WebdavSync(settingsStore, json, context).restoreFromLocalFile(file, settingsStore.settingsFlow.value.webDavConfig)
        }
    }

    suspend fun deleteS3BackupFile(config: S3Config, item: S3BackupItem) = withContext(Dispatchers.IO) {
        val file = File(backupFolder(), item.key)
        if (file.exists()) file.delete()
    }

    private fun backupFolder(): File = File(context.filesDir, "s3_backups").apply { mkdirs() }
}
