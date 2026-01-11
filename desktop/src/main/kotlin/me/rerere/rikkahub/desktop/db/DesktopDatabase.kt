package me.rerere.rikkahub.desktop.db

import me.rerere.backup.BackupLogger
import me.rerere.rikkahub.desktop.backup.DesktopPaths
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

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

    fun listConversations(limit: Int = 50): List<ConversationSummary> {
        val conn = connection ?: return emptyList()
        val sql = """
            SELECT id, title, update_at, is_pinned
            FROM conversationentity
            ORDER BY is_pinned DESC, update_at DESC
            LIMIT ?
        """.trimIndent()
        return runCatching {
            conn.prepareStatement(sql).use { statement ->
                statement.setInt(1, limit)
                statement.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(rs.toConversationSummary())
                        }
                    }
                }
            }
        }.getOrElse { error ->
            logger.error("failed to load conversations", error)
            emptyList()
        }
    }
}

data class ConversationSummary(
    val id: String,
    val title: String,
    val updateAt: Long,
    val isPinned: Boolean,
)

private fun ResultSet.toConversationSummary(): ConversationSummary {
    return ConversationSummary(
        id = getString("id"),
        title = getString("title").ifBlank { "Untitled" },
        updateAt = getLong("update_at"),
        isPinned = getInt("is_pinned") != 0,
    )
}
