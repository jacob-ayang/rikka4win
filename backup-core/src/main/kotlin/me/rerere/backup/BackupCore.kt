package me.rerere.backup

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

enum class BackupItem {
    DATABASE,
    FILES,
}

data class BackupPaths(
    val cacheDir: File,
    val settingsJsonProvider: () -> String,
    val settingsJsonConsumer: (String) -> Unit,
    val dbFile: File,
    val dbWalFile: File,
    val dbShmFile: File,
    val uploadDir: File,
)

data class BackupMetadata(
    val timestamp: String = defaultTimestamp(),
)

interface BackupLogger {
    fun info(message: String)
    fun warn(message: String)
    fun error(message: String, throwable: Throwable? = null)
}

object NoopBackupLogger : BackupLogger {
    override fun info(message: String) = Unit
    override fun warn(message: String) = Unit
    override fun error(message: String, throwable: Throwable?) = Unit
}

object BackupCore {
    fun createBackupZip(
        paths: BackupPaths,
        items: Set<BackupItem>,
        metadata: BackupMetadata = BackupMetadata(),
        logger: BackupLogger = NoopBackupLogger,
    ): File {
        val backupFile = File(paths.cacheDir, "backup_${metadata.timestamp}.zip")
        if (backupFile.exists()) {
            backupFile.delete()
        }

        ZipOutputStream(FileOutputStream(backupFile)).use { zipOut ->
            addVirtualFileToZip(
                zipOut = zipOut,
                name = "settings.json",
                content = paths.settingsJsonProvider()
            )

            if (items.contains(BackupItem.DATABASE)) {
                addOptionalFileToZip(zipOut, paths.dbFile, "rikka_hub.db", logger)
                addOptionalFileToZip(zipOut, paths.dbWalFile, "rikka_hub-wal", logger)
                addOptionalFileToZip(zipOut, paths.dbShmFile, "rikka_hub-shm", logger)
            }

            if (items.contains(BackupItem.FILES)) {
                val uploadFolder = paths.uploadDir
                if (uploadFolder.exists() && uploadFolder.isDirectory) {
                    uploadFolder.listFiles()?.forEach { file ->
                        if (file.isFile) {
                            addFileToZip(zipOut, file, "upload/${file.name}")
                        }
                    }
                } else {
                    logger.warn("upload folder missing or not a directory: ${uploadFolder.absolutePath}")
                }
            }
        }

        logger.info("backup created: ${backupFile.absolutePath}")
        return backupFile
    }

    fun restoreFromZip(
        backupFile: File,
        paths: BackupPaths,
        items: Set<BackupItem>,
        logger: BackupLogger = NoopBackupLogger,
    ) {
        require(backupFile.exists()) { "backup file not found: ${backupFile.absolutePath}" }

        ZipInputStream(FileInputStream(backupFile)).use { zipIn ->
            var entry: ZipEntry?
            while (zipIn.nextEntry.also { entry = it } != null) {
                val zipEntry = entry ?: continue
                when (zipEntry.name) {
                    "settings.json" -> {
                        val settingsJson = zipIn.readBytes().toString(Charsets.UTF_8)
                        paths.settingsJsonConsumer(settingsJson)
                        logger.info("settings restored")
                    }

                    "rikka_hub.db", "rikka_hub-wal", "rikka_hub-shm" -> {
                        if (items.contains(BackupItem.DATABASE)) {
                            val targetFile = when (zipEntry.name) {
                                "rikka_hub.db" -> paths.dbFile
                                "rikka_hub-wal" -> paths.dbWalFile
                                "rikka_hub-shm" -> paths.dbShmFile
                                else -> null
                            }
                            if (targetFile != null) {
                                targetFile.parentFile?.mkdirs()
                                FileOutputStream(targetFile).use { outputStream ->
                                    zipIn.copyTo(outputStream)
                                }
                                logger.info("db file restored: ${zipEntry.name}")
                            }
                        }
                    }

                    else -> {
                        if (items.contains(BackupItem.FILES) && zipEntry.name.startsWith("upload/")) {
                            val fileName = zipEntry.name.substringAfter("upload/")
                            if (fileName.isNotEmpty()) {
                                val uploadFolder = paths.uploadDir
                                if (!uploadFolder.exists()) {
                                    uploadFolder.mkdirs()
                                }
                                val targetFile = File(uploadFolder, fileName)
                                FileOutputStream(targetFile).use { outputStream ->
                                    zipIn.copyTo(outputStream)
                                }
                                logger.info("upload file restored: ${zipEntry.name}")
                            }
                        } else {
                            logger.info("skipping entry: ${zipEntry.name}")
                        }
                    }
                }
                zipIn.closeEntry()
            }
        }
    }

    private fun addOptionalFileToZip(
        zipOut: ZipOutputStream,
        file: File,
        entryName: String,
        logger: BackupLogger,
    ) {
        if (file.exists()) {
            addFileToZip(zipOut, file, entryName)
        } else {
            logger.warn("missing file: $entryName")
        }
    }

    private fun addFileToZip(zipOut: ZipOutputStream, file: File, entryName: String) {
        FileInputStream(file).use { fis ->
            val zipEntry = ZipEntry(entryName)
            zipOut.putNextEntry(zipEntry)
            fis.copyTo(zipOut)
            zipOut.closeEntry()
        }
    }

    private fun addVirtualFileToZip(zipOut: ZipOutputStream, name: String, content: String) {
        val zipEntry = ZipEntry(name)
        zipOut.putNextEntry(zipEntry)
        zipOut.write(content.toByteArray())
        zipOut.closeEntry()
    }

}

private fun defaultTimestamp(): String {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
}
