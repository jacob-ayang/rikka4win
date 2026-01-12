package me.rerere.rikkahub.data.db

import android.content.Context
import java.sql.Connection
import java.sql.DriverManager

class DesktopDatabase(context: Context) {
    private val lock = Any()
    private val connection: Connection

    init {
        val dbFile = context.getDatabasePath("rikka_hub")
        dbFile.parentFile?.mkdirs()
        connection = DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}")
        connection.createStatement().use { statement ->
            statement.execute("PRAGMA foreign_keys = ON")
            statement.execute("PRAGMA journal_mode = WAL")
        }
        initSchema()
    }

    fun <T> query(block: (Connection) -> T): T = synchronized(lock) { block(connection) }

    fun <T> transaction(block: (Connection) -> T): T = synchronized(lock) {
        val originalAutoCommit = connection.autoCommit
        connection.autoCommit = false
        try {
            val result = block(connection)
            connection.commit()
            result
        } catch (e: Exception) {
            connection.rollback()
            throw e
        } finally {
            connection.autoCommit = originalAutoCommit
        }
    }

    private fun initSchema() {
        query { conn ->
            conn.createStatement().use { statement ->
                statement.execute(
                    """
                    CREATE TABLE IF NOT EXISTS conversationentity (
                        id TEXT NOT NULL PRIMARY KEY,
                        assistant_id TEXT NOT NULL DEFAULT '0950e2dc-9bd5-4801-afa3-aa887aa36b4e',
                        title TEXT NOT NULL,
                        nodes TEXT NOT NULL DEFAULT '[]',
                        create_at INTEGER NOT NULL,
                        update_at INTEGER NOT NULL,
                        truncate_index INTEGER NOT NULL DEFAULT -1,
                        suggestions TEXT NOT NULL DEFAULT '[]',
                        is_pinned INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                statement.execute(
                    """
                    CREATE TABLE IF NOT EXISTS memoryentity (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        assistant_id TEXT NOT NULL,
                        content TEXT NOT NULL DEFAULT ''
                    )
                    """.trimIndent()
                )
                statement.execute(
                    """
                    CREATE TABLE IF NOT EXISTS genmediaentity (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        path TEXT NOT NULL,
                        model_id TEXT NOT NULL,
                        prompt TEXT NOT NULL,
                        create_at INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                statement.execute(
                    """
                    CREATE TABLE IF NOT EXISTS message_node (
                        id TEXT NOT NULL PRIMARY KEY,
                        conversation_id TEXT NOT NULL,
                        node_index INTEGER NOT NULL,
                        messages TEXT NOT NULL,
                        select_index INTEGER NOT NULL,
                        FOREIGN KEY (conversation_id) REFERENCES conversationentity(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                statement.execute(
                    "CREATE INDEX IF NOT EXISTS index_message_node_conversation_id ON message_node(conversation_id)"
                )
            }
        }
    }
}
