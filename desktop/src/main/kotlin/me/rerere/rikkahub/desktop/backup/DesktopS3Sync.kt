package me.rerere.rikkahub.desktop.backup

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.rerere.backup.BackupItem
import me.rerere.backup.BackupLogger
import me.rerere.rikkahub.desktop.settings.S3Config
import java.io.File
import java.io.FileOutputStream
import java.time.Instant

class DesktopS3Sync(
    private val backupManager: DesktopBackupManager,
    private val logger: BackupLogger,
) {
    private val httpClient = HttpClient(OkHttp) {
        engine {
            config {
                retryOnConnectionFailure(true)
            }
        }
    }

    suspend fun backup(config: S3Config) = withContext(Dispatchers.IO) {
        val items = config.items.toBackupItems()
        val backupFile = backupManager.createBackup(items)
        val client = DesktopS3Client(config, httpClient)
        val key = "rikkahub_backups/${backupFile.name}"
        client.putObject(key, backupFile.readBytes(), contentType = "application/zip").getOrThrow()
        logger.info("s3 backup uploaded: $key")
        backupFile.delete()
    }

    suspend fun listBackups(config: S3Config): List<S3BackupItem> = withContext(Dispatchers.IO) {
        val client = DesktopS3Client(config, httpClient)
        val objects = client.listObjects(prefix = "rikkahub_backups/").getOrThrow()
        objects
            .filter { it.key.startsWith("rikkahub_backups/backup_") && it.key.endsWith(".zip") }
            .map {
                S3BackupItem(
                    key = it.key,
                    displayName = it.key.substringAfterLast("/"),
                    size = it.size,
                    lastModified = it.lastModified ?: Instant.EPOCH,
                )
            }
            .sortedByDescending { it.lastModified }
    }

    suspend fun restoreLatest(config: S3Config): S3BackupItem? = withContext(Dispatchers.IO) {
        val latest = listBackups(config).firstOrNull() ?: return@withContext null
        restore(config, latest)
        latest
    }

    suspend fun restore(config: S3Config, item: S3BackupItem) = withContext(Dispatchers.IO) {
        val client = DesktopS3Client(config, httpClient)
        val tempFile = File(backupManager.paths.cacheDir, item.displayName)
        if (tempFile.exists()) tempFile.delete()
        val data = client.getObject(item.key).getOrThrow()
        FileOutputStream(tempFile).use { it.write(data) }
        try {
            backupManager.restoreBackup(tempFile, config.items.toBackupItems())
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }
    }

    suspend fun delete(config: S3Config, item: S3BackupItem) = withContext(Dispatchers.IO) {
        val client = DesktopS3Client(config, httpClient)
        client.deleteObject(item.key).getOrThrow()
    }
}

data class S3BackupItem(
    val key: String,
    val displayName: String,
    val size: Long,
    val lastModified: Instant,
)

private fun List<S3Config.BackupItem>.toBackupItems(): Set<BackupItem> {
    return mapNotNull { item ->
        when (item) {
            S3Config.BackupItem.DATABASE -> BackupItem.DATABASE
            S3Config.BackupItem.FILES -> BackupItem.FILES
        }
    }.toSet()
}

internal val S3Config.host: String
    get() = endpoint
        .removePrefix("https://")
        .removePrefix("http://")
        .trimEnd('/')

internal val S3Config.isHttps: Boolean
    get() = endpoint.startsWith("https://")
