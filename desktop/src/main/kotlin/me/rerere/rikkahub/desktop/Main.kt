package me.rerere.rikkahub.desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import me.rerere.rikkahub.desktop.ui.BackupPanel
import me.rerere.rikkahub.desktop.ui.ChatPanel
import me.rerere.rikkahub.desktop.ui.DesktopSection
import me.rerere.rikkahub.desktop.ui.HistoryPanel
import me.rerere.rikkahub.desktop.ui.NavPanel
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
    var section by remember { mutableStateOf(DesktopSection.CHAT) }
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(200.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "RikkaHub Desktop",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            NavPanel(active = section, onSelect = { section = it })
            Button(onClick = {
                val currentIndex = presetThemeIds.indexOf(settings.themeId).coerceAtLeast(0)
                val nextTheme = presetThemeIds[(currentIndex + 1) % presetThemeIds.size]
                settingsStore.update { it.copy(themeId = nextTheme) }
                onSettingsChanged(settingsStore.settings)
            }) {
                Text("Cycle theme")
            }
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start,
        ) {
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
            when (section) {
                DesktopSection.CHAT -> ChatPanel()
                DesktopSection.HISTORY -> HistoryPanel()
                DesktopSection.PROVIDERS -> ProvidersPanel(settings = settings)
                DesktopSection.BACKUP -> BackupPanel(
                    status = status,
                    onStatusChange = { status = it },
                    onBackup = {
                        val backupFile = backupManager.createBackup(setOf(BackupItem.DATABASE, BackupItem.FILES))
                        val target = selectSaveFile()
                        if (target != null) backupFile.copyTo(target, overwrite = true)
                    },
                    onRestore = { file ->
                        database.close()
                        backupManager.restoreBackup(file, setOf(BackupItem.DATABASE, BackupItem.FILES))
                        onSettingsChanged(settingsStore.load())
                        database.open()
                    },
                    onWebDavBackup = { webDavSync.backup(settings.webDavConfig) },
                    onWebDavRestoreLatest = { webDavSync.restoreLatest(settings.webDavConfig) },
                    onS3Backup = { s3Sync.backup(settings.s3Config) },
                    onS3RestoreLatest = { s3Sync.restoreLatest(settings.s3Config) },
                )
            }
        }
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
