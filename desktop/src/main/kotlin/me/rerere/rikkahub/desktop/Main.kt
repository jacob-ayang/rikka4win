package me.rerere.rikkahub.desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.rerere.backup.BackupItem
import me.rerere.backup.BackupLogger
import me.rerere.rikkahub.desktop.backup.DesktopBackupManager
import me.rerere.rikkahub.desktop.backup.DesktopPaths
import me.rerere.rikkahub.desktop.backup.DesktopS3Sync
import me.rerere.rikkahub.desktop.backup.DesktopWebDavSync
import me.rerere.rikkahub.desktop.db.DesktopDatabase
import kotlinx.serialization.json.JsonArray
import me.rerere.rikkahub.desktop.settings.DesktopSettingsStore
import me.rerere.rikkahub.desktop.settings.totalModelCount
import me.rerere.rikkahub.desktop.theme.RikkahubDesktopTheme
import me.rerere.rikkahub.desktop.theme.presetThemeIds
import me.rerere.rikkahub.desktop.ui.ProvidersPanel
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "RikkaHub",
    ) {
        val paths = remember { DesktopPaths() }
        val settingsStore = remember { DesktopSettingsStore(paths) }
        var settings by remember { mutableStateOf(settingsStore.load()) }
        RikkahubDesktopTheme(themeId = settings.themeId) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                DesktopHome(
                    paths = paths,
                    settingsStore = settingsStore,
                    settings = settings,
                    onSettingsChanged = { settings = it },
                )
            }
        }
    }
}

@Composable
private fun DesktopHome(
    paths: DesktopPaths,
    settingsStore: DesktopSettingsStore,
    settings: me.rerere.rikkahub.desktop.settings.DesktopSettings,
    onSettingsChanged: (me.rerere.rikkahub.desktop.settings.DesktopSettings) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("Ready") }
    val logger = remember { ConsoleBackupLogger() }
    val backupManager = remember { DesktopBackupManager(paths, logger) }
    val s3Sync = remember { DesktopS3Sync(backupManager, logger) }
    val webDavSync = remember { DesktopWebDavSync(backupManager, logger) }
    val database = remember { DesktopDatabase(paths, logger) }
    DisposableEffect(Unit) {
        database.open()
        onDispose { database.close() }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "RikkaHub Desktop",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Desktop build is wiring up core logic and backup compatibility.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        val providerCount = (settings.providers as? JsonArray)?.size ?: 0
        Text(
            text = "Theme: ${settings.themeId}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Providers: $providerCount, Models: ${settings.totalModelCount()}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        ProvidersPanel(settings = settings)
        Button(onClick = {
            val currentIndex = presetThemeIds.indexOf(settings.themeId).coerceAtLeast(0)
            val nextTheme = presetThemeIds[(currentIndex + 1) % presetThemeIds.size]
            settingsStore.update { it.copy(themeId = nextTheme) }
            onSettingsChanged(settingsStore.settings)
        }) {
            Text("Cycle theme")
        }
        Button(onClick = {
            scope.launch(Dispatchers.IO) {
                runCatching {
                    status = "Creating backup..."
                    val backupFile = backupManager.createBackup(setOf(BackupItem.DATABASE, BackupItem.FILES))
                    val target = selectSaveFile() ?: run {
                        status = "Backup canceled"
                        return@launch
                    }
                    backupFile.copyTo(target, overwrite = true)
                    status = "Backup saved: ${target.name}"
                }.onFailure { error ->
                    status = "Backup failed: ${error.message}"
                }
            }
        }) {
            Text("Create backup zip")
        }
        Button(onClick = {
            scope.launch(Dispatchers.IO) {
                runCatching {
                    val source = selectOpenFile() ?: run {
                        status = "Restore canceled"
                        return@launch
                    }
                    status = "Restoring backup..."
                    database.close()
                    backupManager.restoreBackup(source, setOf(BackupItem.DATABASE, BackupItem.FILES))
                    onSettingsChanged(settingsStore.load())
                    database.open()
                    status = "Restore finished"
                }.onFailure { error ->
                    status = "Restore failed: ${error.message}"
                }
            }
        }) {
            Text("Restore from backup zip")
        }
        Button(onClick = {
            scope.launch(Dispatchers.IO) {
                runCatching {
                    val config = settings.webDavConfig
                    if (config.url.isBlank()) {
                        status = "WebDAV config missing"
                        return@launch
                    }
                    status = "WebDAV backup..."
                    webDavSync.backup(config)
                    status = "WebDAV backup uploaded"
                }.onFailure { error ->
                    status = "WebDAV backup failed: ${error.message}"
                }
            }
        }) {
            Text("WebDAV backup")
        }
        Button(onClick = {
            scope.launch(Dispatchers.IO) {
                runCatching {
                    val config = settings.webDavConfig
                    if (config.url.isBlank()) {
                        status = "WebDAV config missing"
                        return@launch
                    }
                    status = "WebDAV restore latest..."
                    database.close()
                    val restored = webDavSync.restoreLatest(config)
                    onSettingsChanged(settingsStore.load())
                    database.open()
                    status = if (restored != null) {
                        "WebDAV restored: ${restored.displayName}"
                    } else {
                        "No WebDAV backups found"
                    }
                }.onFailure { error ->
                    status = "WebDAV restore failed: ${error.message}"
                }
            }
        }) {
            Text("WebDAV restore latest")
        }
        Button(onClick = {
            scope.launch(Dispatchers.IO) {
                runCatching {
                    val config = settings.s3Config
                    if (config.endpoint.isBlank() || config.bucket.isBlank()) {
                        status = "S3 config missing"
                        return@launch
                    }
                    status = "S3 backup..."
                    s3Sync.backup(config)
                    status = "S3 backup uploaded"
                }.onFailure { error ->
                    status = "S3 backup failed: ${error.message}"
                }
            }
        }) {
            Text("S3 backup")
        }
        Button(onClick = {
            scope.launch(Dispatchers.IO) {
                runCatching {
                    val config = settings.s3Config
                    if (config.endpoint.isBlank() || config.bucket.isBlank()) {
                        status = "S3 config missing"
                        return@launch
                    }
                    status = "S3 restore latest..."
                    database.close()
                    val restored = s3Sync.restoreLatest(config)
                    onSettingsChanged(settingsStore.load())
                    database.open()
                    status = if (restored != null) {
                        "S3 restored: ${restored.displayName}"
                    } else {
                        "No S3 backups found"
                    }
                }.onFailure { error ->
                    status = "S3 restore failed: ${error.message}"
                }
            }
        }) {
            Text("S3 restore latest")
        }
        Text(
            text = status,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

private class ConsoleBackupLogger : BackupLogger {
    override fun info(message: String) = println(message)
    override fun warn(message: String) = println("WARN: $message")
    override fun error(message: String, throwable: Throwable?) {
        println("ERROR: ${throwable?.message ?: message}")
    }
}

private fun selectSaveFile(): File? {
    val dialog = FileDialog(null as Frame?, "Save backup", FileDialog.SAVE)
    dialog.isVisible = true
    val directory = dialog.directory ?: return null
    val file = dialog.file ?: return null
    return File(directory, file)
}

private fun selectOpenFile(): File? {
    val dialog = FileDialog(null as Frame?, "Open backup", FileDialog.LOAD)
    dialog.isVisible = true
    val directory = dialog.directory ?: return null
    val file = dialog.file ?: return null
    return File(directory, file)
}
