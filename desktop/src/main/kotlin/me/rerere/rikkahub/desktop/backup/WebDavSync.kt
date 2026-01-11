package me.rerere.rikkahub.desktop.backup

import at.bitfire.dav4jvm.okhttp.BasicDigestAuthHandler
import at.bitfire.dav4jvm.okhttp.DavCollection
import at.bitfire.dav4jvm.okhttp.Response
import at.bitfire.dav4jvm.okhttp.exception.NotFoundException
import at.bitfire.dav4jvm.property.webdav.DisplayName
import at.bitfire.dav4jvm.property.webdav.GetContentLength
import at.bitfire.dav4jvm.property.webdav.GetLastModified
import at.bitfire.dav4jvm.property.webdav.WebDAV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.rerere.backup.BackupItem
import me.rerere.backup.BackupLogger
import me.rerere.rikkahub.desktop.settings.WebDavConfig
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.util.concurrent.TimeUnit

class DesktopWebDavSync(
    private val backupManager: DesktopBackupManager,
    private val logger: BackupLogger,
) {
    suspend fun test(config: WebDavConfig) = withContext(Dispatchers.IO) {
        val collection = config.requireCollection()
        collection.propfind(
            depth = 1,
            WebDAV.DisplayName,
        ) { response, relation ->
            logger.info("test webdav: $response | $relation")
        }
    }

    suspend fun backup(config: WebDavConfig) = withContext(Dispatchers.IO) {
        val items = config.items.toBackupItems()
        val backupFile = backupManager.createBackup(items)
        val collection = config.requireCollection()
        collection.ensureCollectionExists(logger)
        val target = config.requireCollection(backupFile.name)
        target.put(body = backupFile.asRequestBody()) { response ->
            logger.info("webdav backup: $response")
        }
    }

    suspend fun listBackups(config: WebDavConfig): List<WebDavBackupItem> = withContext(Dispatchers.IO) {
        val collection = config.requireCollection()
        val files = mutableListOf<WebDavBackupItem>()
        collection.propfind(
            depth = 1,
            WebDAV.DisplayName,
            WebDAV.GetContentLength,
            WebDAV.GetLastModified,
        ) { response, relation ->
            if (relation == Response.HrefRelation.MEMBER) {
                val displayName = response.properties.filterIsInstance<DisplayName>()
                    .firstOrNull()?.displayName ?: "Unknown"
                val size = response.properties.filterIsInstance<GetContentLength>()
                    .firstOrNull()?.contentLength ?: 0L
                val lastModified = response.properties.filterIsInstance<GetLastModified>()
                    .firstOrNull()?.lastModified ?: Instant.EPOCH
                files.add(
                    WebDavBackupItem(
                        href = response.href.toString(),
                        displayName = displayName,
                        size = size,
                        lastModified = lastModified,
                    )
                )
            }
        }
        files
    }

    suspend fun restoreLatest(config: WebDavConfig): WebDavBackupItem? = withContext(Dispatchers.IO) {
        val items = listBackups(config)
        val latest = items.maxByOrNull { it.lastModified } ?: return@withContext null
        restore(config, latest)
        latest
    }

    suspend fun restore(config: WebDavConfig, item: WebDavBackupItem) = withContext(Dispatchers.IO) {
        val collection = DavCollection(
            httpClient = config.requireClient(),
            location = item.href.toHttpUrl(),
        )
        val tempFile = File(backupManager.paths.cacheDir, item.displayName)
        if (tempFile.exists()) tempFile.delete()

        collection.get(accept = "", headers = null) { response ->
            if (response.isSuccessful) {
                response.body?.byteStream()?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } else {
                throw IllegalStateException("Failed to download backup: ${response.message}")
            }
        }

        try {
            backupManager.restoreBackup(tempFile, config.items.toBackupItems())
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }
    }

    suspend fun delete(config: WebDavConfig, item: WebDavBackupItem) = withContext(Dispatchers.IO) {
        val collection = DavCollection(
            httpClient = config.requireClient(),
            location = item.href.toHttpUrl(),
        )
        collection.delete { response ->
            logger.info("webdav delete: $response")
        }
    }
}

data class WebDavBackupItem(
    val href: String,
    val displayName: String,
    val size: Long,
    val lastModified: Instant,
)

private fun WebDavConfig.requireClient(): OkHttpClient {
    val authHandler = BasicDigestAuthHandler(
        domain = null,
        username = this.username,
        password = this.password.toCharArray(),
    )
    return OkHttpClient.Builder()
        .followRedirects(false)
        .authenticator(authHandler)
        .addNetworkInterceptor(authHandler)
        .writeTimeout(5, TimeUnit.MINUTES)
        .build()
}

private fun WebDavConfig.requireCollection(path: String? = null): DavCollection {
    val location = buildString {
        append(this@requireCollection.url.trimEnd('/'))
        append("/")
        if (this@requireCollection.path.isNotBlank()) {
            append(this@requireCollection.path.trim('/'))
            append("/")
        }
        if (path != null) {
            append(path.trim('/'))
        }
    }.toHttpUrl()
    return DavCollection(
        httpClient = this.requireClient(),
        location = location,
    )
}

private suspend fun DavCollection.ensureCollectionExists(logger: BackupLogger) = withContext(Dispatchers.IO) {
    try {
        propfind(depth = 0, WebDAV.DisplayName) { response, relation ->
            logger.info("webdav collection ok: $response $relation")
        }
    } catch (error: NotFoundException) {
        mkCol(null) { response ->
            logger.info("webdav mkcol: $response")
        }
    }
}

private fun List<WebDavConfig.BackupItem>.toBackupItems(): Set<BackupItem> {
    return mapNotNull { item ->
        when (item) {
            WebDavConfig.BackupItem.DATABASE -> BackupItem.DATABASE
            WebDavConfig.BackupItem.FILES -> BackupItem.FILES
        }
    }.toSet()
}
