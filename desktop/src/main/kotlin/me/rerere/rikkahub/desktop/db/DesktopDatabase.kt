package me.rerere.rikkahub.desktop.db

import me.rerere.backup.BackupLogger
import me.rerere.rikkahub.desktop.backup.DesktopPaths
import java.sql.Connection
import java.sql.DriverManager

class DesktopDatabase(
    private val paths: DesktopPaths,
    private val logger: BackupLogger,
) {
    private var connection: Connection? = null

    fun open() {
        if (connection != null) return
        paths.ensureDirs()
        val jdbcUrl = "jdbc:sqlite:${paths.dbFile.absolutePath}"
        try {
            connection = DriverManager.getConnection(jdbcUrl)
            connection?.createStatement()?.use { statement ->
                statement.execute("PRAGMA journal_mode=WAL;")
                statement.execute("PRAGMA foreign_keys=ON;")
            }
            logger.info("database opened: ${paths.dbFile.absolutePath}")
        } catch (error: Exception) {
            logger.error("failed to open database", error)
            connection?.close()
            connection = null
        }
    }

    fun close() {
        runCatching {
            connection?.close()
        }
        connection = null
    }
}
