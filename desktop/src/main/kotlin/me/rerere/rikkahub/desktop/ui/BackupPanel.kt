package me.rerere.rikkahub.desktop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun BackupPanel(
    status: String,
    onStatusChange: (String) -> Unit,
    onBackup: () -> Unit,
    onRestore: (File) -> Unit,
    onWebDavBackup: suspend () -> Unit,
    onWebDavRestoreLatest: suspend () -> Any?,
    onS3Backup: suspend () -> Unit,
    onS3RestoreLatest: suspend () -> Any?,
) {
    val scope = rememberCoroutineScope()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Backup",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Button(onClick = {
                scope.launch(Dispatchers.IO) {
                    runCatching {
                        onStatusChange("Creating backup...")
                        onBackup()
                        onStatusChange("Backup saved")
                    }.onFailure { error ->
                        onStatusChange("Backup failed: ${error.message}")
                    }
                }
            }) {
                Text("Create backup zip")
            }
            Button(onClick = {
                scope.launch(Dispatchers.IO) {
                    runCatching {
                        onStatusChange("Restoring backup...")
                        val file = chooseFileToOpen() ?: run {
                            onStatusChange("Restore canceled")
                            return@launch
                        }
                        onRestore(file)
                        onStatusChange("Restore finished")
                    }.onFailure { error ->
                        onStatusChange("Restore failed: ${error.message}")
                    }
                }
            }) {
                Text("Restore from backup zip")
            }
            Button(onClick = {
                scope.launch(Dispatchers.IO) {
                    runCatching {
                        onStatusChange("WebDAV backup...")
                        onWebDavBackup()
                        onStatusChange("WebDAV backup uploaded")
                    }.onFailure { error ->
                        onStatusChange("WebDAV backup failed: ${error.message}")
                    }
                }
            }) {
                Text("WebDAV backup")
            }
            Button(onClick = {
                scope.launch(Dispatchers.IO) {
                    runCatching {
                        onStatusChange("WebDAV restore latest...")
                        val restored = onWebDavRestoreLatest()
                        onStatusChange(if (restored != null) "WebDAV restored" else "No WebDAV backups found")
                    }.onFailure { error ->
                        onStatusChange("WebDAV restore failed: ${error.message}")
                    }
                }
            }) {
                Text("WebDAV restore latest")
            }
            Button(onClick = {
                scope.launch(Dispatchers.IO) {
                    runCatching {
                        onStatusChange("S3 backup...")
                        onS3Backup()
                        onStatusChange("S3 backup uploaded")
                    }.onFailure { error ->
                        onStatusChange("S3 backup failed: ${error.message}")
                    }
                }
            }) {
                Text("S3 backup")
            }
            Button(onClick = {
                scope.launch(Dispatchers.IO) {
                    runCatching {
                        onStatusChange("S3 restore latest...")
                        val restored = onS3RestoreLatest()
                        onStatusChange(if (restored != null) "S3 restored" else "No S3 backups found")
                    }.onFailure { error ->
                        onStatusChange("S3 restore failed: ${error.message}")
                    }
                }
            }) {
                Text("S3 restore latest")
            }
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun chooseFileToOpen(): File? {
    val dialog = java.awt.FileDialog(null as java.awt.Frame?, "Open backup", java.awt.FileDialog.LOAD)
    dialog.isVisible = true
    val directory = dialog.directory ?: return null
    val file = dialog.file ?: return null
    return File(directory, file)
}
