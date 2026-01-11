package me.rerere.rikkahub.desktop.db

import me.rerere.backup.BackupLogger
import me.rerere.rikkahub.desktop.backup.DesktopPaths
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

class DesktopDatabase(
    private val paths: DesktopPaths,
    private val logger: BackupLogger,
) {
    private var connection: Connection? = null
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

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

    fun listConversationMessages(conversationId: String): List<DisplayMessage> {
        val conn = connection ?: return emptyList()
        val sql = """
            SELECT messages, select_index
            FROM message_node
            WHERE conversation_id = ?
            ORDER BY node_index ASC
        """.trimIndent()
        return runCatching {
            conn.prepareStatement(sql).use { statement ->
                statement.setString(1, conversationId)
                statement.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            val messagesJson = rs.getString("messages")
                            val selectIndex = rs.getInt("select_index")
                            val message = parseSelectedMessage(messagesJson, selectIndex)
                            if (message != null) add(message)
                        }
                    }
                }
            }
        }.getOrElse { error ->
            logger.error("failed to load messages", error)
            emptyList()
        }
    }

    fun listMessageNodes(conversationId: String): List<DesktopMessageNode> {
        val conn = connection ?: return emptyList()
        val sql = """
            SELECT id, node_index, select_index, messages
            FROM message_node
            WHERE conversation_id = ?
            ORDER BY node_index ASC
        """.trimIndent()
        return runCatching {
            conn.prepareStatement(sql).use { statement ->
                statement.setString(1, conversationId)
                statement.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            val messagesJson = rs.getString("messages")
                            val selectIndex = rs.getInt("select_index")
                            val messages = parseAllMessages(messagesJson)
                            add(
                                DesktopMessageNode(
                                    id = rs.getString("id"),
                                    nodeIndex = rs.getInt("node_index"),
                                    selectIndex = selectIndex,
                                    messages = messages,
                                )
                            )
                        }
                    }
                }
            }
        }.getOrElse { error ->
            logger.error("failed to load message nodes", error)
            emptyList()
        }
    }

    fun updateSelectIndex(nodeId: String, selectIndex: Int): Boolean {
        val conn = connection ?: return false
        val sql = "UPDATE message_node SET select_index = ? WHERE id = ?"
        return runCatching {
            conn.prepareStatement(sql).use { statement ->
                statement.setInt(1, selectIndex)
                statement.setString(2, nodeId)
                statement.executeUpdate() > 0
            }
        }.getOrElse { error ->
            logger.error("failed to update select_index", error)
            false
        }
    }

    private fun parseSelectedMessage(messagesJson: String, selectIndex: Int): DisplayMessage? {
        val array = json.parseToJsonElement(messagesJson) as? JsonArray ?: return null
        if (array.isEmpty()) return null
        val messageIndex = if (selectIndex in array.indices) selectIndex else 0
        val messageObj = array[messageIndex] as? JsonObject ?: return null
        val role = messageObj["role"]?.jsonPrimitive?.contentOrNull ?: "UNKNOWN"
        val parts = messageObj["parts"] as? JsonArray ?: return DisplayMessage(role, "[Empty]", emptyList())
        val contents = parts.mapNotNull { part ->
            val obj = part as? JsonObject ?: return@mapNotNull null
            when {
                obj["text"]?.jsonPrimitive?.contentOrNull != null -> MessageContent.Text(
                    obj["text"]?.jsonPrimitive?.contentOrNull.orEmpty()
                )

                obj["reasoning"]?.jsonPrimitive?.contentOrNull != null -> MessageContent.Reasoning(
                    obj["reasoning"]?.jsonPrimitive?.contentOrNull.orEmpty()
                )

                obj["toolName"]?.jsonPrimitive?.contentOrNull != null -> {
                    val tool = obj["toolName"]?.jsonPrimitive?.contentOrNull
                    MessageContent.Tool(tool ?: "Tool")
                }

                obj["url"]?.jsonPrimitive?.contentOrNull != null -> MessageContent.Media(
                    obj["url"]?.jsonPrimitive?.contentOrNull.orEmpty()
                )

                else -> null
            }
        }
        return DisplayMessage(
            role = role,
            text = contents.joinToString(separator = "\n") { it.summary() }.ifBlank { "[Empty]" },
            contents = contents,
        )
    }

    private fun parseAllMessages(messagesJson: String): List<DisplayMessage> {
        val array = json.parseToJsonElement(messagesJson) as? JsonArray ?: return emptyList()
        return array.mapNotNull { element ->
            val messageObj = element as? JsonObject ?: return@mapNotNull null
            val role = messageObj["role"]?.jsonPrimitive?.contentOrNull ?: "UNKNOWN"
            val parts = messageObj["parts"] as? JsonArray ?: return@mapNotNull DisplayMessage(role, "[Empty]", emptyList())
            val contents = parts.mapNotNull { part ->
                val obj = part as? JsonObject ?: return@mapNotNull null
                when {
                    obj["text"]?.jsonPrimitive?.contentOrNull != null -> MessageContent.Text(
                        obj["text"]?.jsonPrimitive?.contentOrNull.orEmpty()
                    )

                    obj["reasoning"]?.jsonPrimitive?.contentOrNull != null -> MessageContent.Reasoning(
                        obj["reasoning"]?.jsonPrimitive?.contentOrNull.orEmpty()
                    )

                    obj["toolName"]?.jsonPrimitive?.contentOrNull != null -> {
                        val tool = obj["toolName"]?.jsonPrimitive?.contentOrNull
                        MessageContent.Tool(tool ?: "Tool")
                    }

                    obj["url"]?.jsonPrimitive?.contentOrNull != null -> MessageContent.Media(
                        obj["url"]?.jsonPrimitive?.contentOrNull.orEmpty()
                    )

                    else -> null
                }
            }
            DisplayMessage(
                role = role,
                text = contents.joinToString(separator = "\n") { it.summary() }.ifBlank { "[Empty]" },
                contents = contents,
            )
        }
    }
}

data class ConversationSummary(
    val id: String,
    val title: String,
    val updateAt: Long,
    val isPinned: Boolean,
)

data class DisplayMessage(
    val role: String,
    val text: String,
    val contents: List<MessageContent>,
)

sealed class MessageContent {
    data class Text(val value: String) : MessageContent()
    data class Reasoning(val value: String) : MessageContent()
    data class Tool(val name: String) : MessageContent()
    data class Media(val url: String) : MessageContent()
}

internal fun MessageContent.summary(): String = when (this) {
    is MessageContent.Text -> value
    is MessageContent.Reasoning -> "[Reasoning] $value"
    is MessageContent.Tool -> "[Tool] $name"
    is MessageContent.Media -> "[Media]"
}

private fun ResultSet.toConversationSummary(): ConversationSummary {
    return ConversationSummary(
        id = getString("id"),
        title = getString("title").ifBlank { "Untitled" },
        updateAt = getLong("update_at"),
        isPinned = getInt("is_pinned") != 0,
    )
}
