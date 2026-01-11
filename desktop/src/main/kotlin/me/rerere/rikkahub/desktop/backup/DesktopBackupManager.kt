package me.rerere.rikkahub.desktop.backup

import me.rerere.backup.BackupCore
import me.rerere.backup.BackupItem
import me.rerere.backup.BackupLogger
import me.rerere.backup.BackupMetadata
import me.rerere.backup.BackupPaths
import java.io.File

class DesktopBackupManager(
    private val paths: DesktopPaths,
    private val logger: BackupLogger,
) {
    fun createBackup(items: Set<BackupItem>): File {
        paths.ensureDirs()
        return BackupCore.createBackupZip(
            paths = buildBackupPaths(),
            items = items,
            metadata = BackupMetadata(),
            logger = logger,
        )
    }

    fun restoreBackup(file: File, items: Set<BackupItem>) {
        paths.ensureDirs()
        BackupCore.restoreFromZip(
            backupFile = file,
            paths = buildBackupPaths(),
            items = items,
            logger = logger,
        )
    }

    private fun buildBackupPaths(): BackupPaths {
        return BackupPaths(
            cacheDir = paths.cacheDir,
            settingsJsonProvider = { paths.readSettingsJson() },
            settingsJsonConsumer = { content -> paths.writeSettingsJson(content) },
            dbFile = paths.dbFile,
            dbWalFile = paths.dbWalFile,
            dbShmFile = paths.dbShmFile,
            uploadDir = paths.uploadDir,
        )
    }
}
