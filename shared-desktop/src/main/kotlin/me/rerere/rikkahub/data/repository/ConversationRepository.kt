package me.rerere.rikkahub.data.repository

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.rerere.ai.ui.UIMessage
import me.rerere.rikkahub.data.db.DesktopDatabase
import me.rerere.rikkahub.data.db.entity.ConversationEntity
import me.rerere.rikkahub.data.db.entity.MessageNodeEntity
import me.rerere.rikkahub.data.model.Conversation
import me.rerere.rikkahub.data.model.MessageNode
import me.rerere.rikkahub.utils.JsonInstant
import me.rerere.rikkahub.utils.deleteChatFiles
import java.sql.Connection
import java.time.Instant
import kotlin.uuid.Uuid

class ConversationRepository(
    private val context: Context,
    private val database: DesktopDatabase,
) {
    companion object {
        private const val PAGE_SIZE = 20
        private const val INITIAL_LOAD_SIZE = 40
        private const val NODE_PAGE_SIZE = 64
    }

    private val conversations = MutableStateFlow(loadAllConversations())

    suspend fun getRecentConversations(assistantId: Uuid, limit: Int = 10): List<Conversation> {
        return withContext(Dispatchers.IO) {
            database.query { conn ->
                val results = mutableListOf<Conversation>()
                conn.prepareStatement(
                    """
                    SELECT id, assistant_id, title, nodes, create_at, update_at, truncate_index, suggestions, is_pinned
                    FROM conversationentity
                    WHERE assistant_id = ?
                    ORDER BY is_pinned DESC, update_at DESC
                    LIMIT ?
                    """.trimIndent()
                ).use { statement ->
                    statement.setString(1, assistantId.toString())
                    statement.setInt(2, limit)
                    statement.executeQuery().use { rs ->
                        while (rs.next()) {
                            val entity = rs.toConversationEntity()
                            val nodes = loadMessageNodes(conn, entity.id)
                            results.add(conversationEntityToConversation(entity, nodes))
                        }
                    }
                }
                results
            }
        }
    }

    fun getConversationsOfAssistant(assistantId: Uuid): Flow<List<Conversation>> {
        return conversations.map { list ->
            list.filter { it.assistantId == assistantId.toString() }
                .map { entity -> conversationEntityToConversation(entity, emptyList()) }
        }
    }

    fun getConversationsOfAssistantPaging(assistantId: Uuid): Flow<PagingData<Conversation>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            initialLoadSize = INITIAL_LOAD_SIZE,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            ListPagingSource(
                conversations.value
                    .filter { it.assistantId == assistantId.toString() }
                    .map { entity -> conversationSummaryToConversation(entity) }
            )
        }
    ).flow

    fun searchConversations(titleKeyword: String): Flow<List<Conversation>> {
        return conversations.map { list ->
            list.filter { it.title.contains(titleKeyword, ignoreCase = true) }
                .map { entity -> conversationEntityToConversation(entity, emptyList()) }
        }
    }

    fun searchConversationsPaging(titleKeyword: String): Flow<PagingData<Conversation>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            initialLoadSize = INITIAL_LOAD_SIZE,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            ListPagingSource(
                conversations.value
                    .filter { it.title.contains(titleKeyword, ignoreCase = true) }
                    .map { entity -> conversationSummaryToConversation(entity) }
            )
        }
    ).flow

    fun searchConversationsOfAssistant(assistantId: Uuid, titleKeyword: String): Flow<List<Conversation>> {
        return conversations.map { list ->
            list.filter {
                it.assistantId == assistantId.toString() && it.title.contains(titleKeyword, ignoreCase = true)
            }.map { entity -> conversationEntityToConversation(entity, emptyList()) }
        }
    }

    fun searchConversationsOfAssistantPaging(assistantId: Uuid, titleKeyword: String): Flow<PagingData<Conversation>> =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = INITIAL_LOAD_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                ListPagingSource(
                    conversations.value
                        .filter {
                            it.assistantId == assistantId.toString() &&
                                it.title.contains(titleKeyword, ignoreCase = true)
                        }
                        .map { entity -> conversationSummaryToConversation(entity) }
                )
            }
        ).flow

    suspend fun getConversationById(uuid: Uuid): Conversation? {
        return withContext(Dispatchers.IO) {
            database.query { conn ->
                conn.prepareStatement(
                    """
                    SELECT id, assistant_id, title, nodes, create_at, update_at, truncate_index, suggestions, is_pinned
                    FROM conversationentity
                    WHERE id = ?
                    """.trimIndent()
                ).use { statement ->
                    statement.setString(1, uuid.toString())
                    statement.executeQuery().use { rs ->
                        if (!rs.next()) return@query null
                        val entity = rs.toConversationEntity()
                        val nodes = loadMessageNodes(conn, entity.id)
                        conversationEntityToConversation(entity, nodes)
                    }
                }
            }
        }
    }

    suspend fun insertConversation(conversation: Conversation) {
        withContext(Dispatchers.IO) {
            database.transaction { conn ->
                val entity = conversationToConversationEntity(conversation)
                conn.prepareStatement(
                    """
                    INSERT INTO conversationentity
                    (id, assistant_id, title, nodes, create_at, update_at, truncate_index, suggestions, is_pinned)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent()
                ).use { statement ->
                    statement.setString(1, entity.id)
                    statement.setString(2, entity.assistantId)
                    statement.setString(3, entity.title)
                    statement.setString(4, entity.nodes)
                    statement.setLong(5, entity.createAt)
                    statement.setLong(6, entity.updateAt)
                    statement.setInt(7, entity.truncateIndex)
                    statement.setString(8, entity.chatSuggestions)
                    statement.setInt(9, if (entity.isPinned) 1 else 0)
                    statement.executeUpdate()
                }
                saveMessageNodes(conn, conversation.id.toString(), conversation.messageNodes)
            }
            refreshConversations()
        }
    }

    suspend fun updateConversation(conversation: Conversation) {
        withContext(Dispatchers.IO) {
            database.transaction { conn ->
                val entity = conversationToConversationEntity(conversation)
                conn.prepareStatement(
                    """
                    UPDATE conversationentity
                    SET assistant_id = ?, title = ?, nodes = ?, create_at = ?, update_at = ?,
                        truncate_index = ?, suggestions = ?, is_pinned = ?
                    WHERE id = ?
                    """.trimIndent()
                ).use { statement ->
                    statement.setString(1, entity.assistantId)
                    statement.setString(2, entity.title)
                    statement.setString(3, entity.nodes)
                    statement.setLong(4, entity.createAt)
                    statement.setLong(5, entity.updateAt)
                    statement.setInt(6, entity.truncateIndex)
                    statement.setString(7, entity.chatSuggestions)
                    statement.setInt(8, if (entity.isPinned) 1 else 0)
                    statement.setString(9, entity.id)
                    statement.executeUpdate()
                }
                deleteMessageNodes(conn, conversation.id.toString())
                saveMessageNodes(conn, conversation.id.toString(), conversation.messageNodes)
            }
            refreshConversations()
        }
    }

    suspend fun deleteConversation(conversation: Conversation) {
        val fullConversation = if (conversation.messageNodes.isEmpty()) {
            getConversationById(conversation.id) ?: conversation
        } else {
            conversation
        }
        withContext(Dispatchers.IO) {
            database.transaction { conn ->
                conn.prepareStatement("DELETE FROM conversationentity WHERE id = ?").use { statement ->
                    statement.setString(1, conversation.id.toString())
                    statement.executeUpdate()
                }
            }
            refreshConversations()
        }
        context.deleteChatFiles(fullConversation.files)
    }

    suspend fun deleteConversationOfAssistant(assistantId: Uuid) {
        withContext(Dispatchers.IO) {
            database.transaction { conn ->
                conn.prepareStatement("DELETE FROM conversationentity WHERE assistant_id = ?").use { statement ->
                    statement.setString(1, assistantId.toString())
                    statement.executeUpdate()
                }
            }
            refreshConversations()
        }
    }

    fun getPinnedConversations(): Flow<List<Conversation>> {
        return conversations.map { list ->
            list.filter { it.isPinned }
                .map { entity -> conversationEntityToConversation(entity, emptyList()) }
        }
    }

    suspend fun togglePinStatus(conversationId: Uuid) {
        withContext(Dispatchers.IO) {
            val currentPinned = conversations.value
                .firstOrNull { it.id == conversationId.toString() }
                ?.isPinned
                ?: false
            database.query { conn ->
                conn.prepareStatement("UPDATE conversationentity SET is_pinned = ? WHERE id = ?").use { statement ->
                    statement.setInt(1, if (currentPinned) 0 else 1)
                    statement.setString(2, conversationId.toString())
                    statement.executeUpdate()
                }
            }
            refreshConversations()
        }
    }

    private fun refreshConversations() {
        conversations.value = loadAllConversations()
    }

    private fun loadAllConversations(): List<ConversationEntity> {
        return database.query { conn ->
            conn.prepareStatement(
                """
                SELECT id, assistant_id, title, nodes, create_at, update_at, truncate_index, suggestions, is_pinned
                FROM conversationentity
                ORDER BY is_pinned DESC, update_at DESC
                """.trimIndent()
            ).use { statement ->
                statement.executeQuery().use { rs ->
                    val results = mutableListOf<ConversationEntity>()
                    while (rs.next()) {
                        results.add(rs.toConversationEntity())
                    }
                    results
                }
            }
        }
    }

    private fun conversationEntityToConversation(
        conversationEntity: ConversationEntity,
        messageNodes: List<MessageNode>
    ): Conversation {
        return Conversation(
            id = Uuid.parse(conversationEntity.id),
            title = conversationEntity.title,
            messageNodes = messageNodes.filter { it.messages.isNotEmpty() },
            createAt = Instant.ofEpochMilli(conversationEntity.createAt),
            updateAt = Instant.ofEpochMilli(conversationEntity.updateAt),
            assistantId = Uuid.parse(conversationEntity.assistantId),
            truncateIndex = conversationEntity.truncateIndex,
            chatSuggestions = JsonInstant.decodeFromString(conversationEntity.chatSuggestions),
            isPinned = conversationEntity.isPinned,
        )
    }

    private fun conversationSummaryToConversation(entity: ConversationEntity): Conversation {
        return Conversation(
            id = Uuid.parse(entity.id),
            assistantId = Uuid.parse(entity.assistantId),
            title = entity.title,
            isPinned = entity.isPinned,
            createAt = Instant.ofEpochMilli(entity.createAt),
            updateAt = Instant.ofEpochMilli(entity.updateAt),
            messageNodes = emptyList(),
        )
    }

    private fun conversationToConversationEntity(conversation: Conversation): ConversationEntity {
        require(conversation.messageNodes.none { node -> node.messages.any { message -> message.hasBase64Part() } })
        return ConversationEntity(
            id = conversation.id.toString(),
            title = conversation.title,
            nodes = "[]",
            createAt = conversation.createAt.toEpochMilli(),
            updateAt = conversation.updateAt.toEpochMilli(),
            assistantId = conversation.assistantId.toString(),
            truncateIndex = conversation.truncateIndex,
            chatSuggestions = JsonInstant.encodeToString(conversation.chatSuggestions),
            isPinned = conversation.isPinned
        )
    }

    private fun loadMessageNodes(conn: Connection, conversationId: String): List<MessageNode> {
        val nodes = mutableListOf<MessageNode>()
        var offset = 0
        while (true) {
            val page = loadMessageNodePage(conn, conversationId, NODE_PAGE_SIZE, offset)
            if (page.isEmpty()) break
            page.forEach { entity ->
                nodes.add(
                    MessageNode(
                        id = Uuid.parse(entity.id),
                        messages = JsonInstant.decodeFromString<List<UIMessage>>(entity.messages),
                        selectIndex = entity.selectIndex
                    )
                )
            }
            offset += page.size
        }
        return nodes
    }

    private fun loadMessageNodePage(
        conn: Connection,
        conversationId: String,
        limit: Int,
        offset: Int
    ): List<MessageNodeEntity> {
        conn.prepareStatement(
            """
            SELECT id, conversation_id, node_index, messages, select_index
            FROM message_node
            WHERE conversation_id = ?
            ORDER BY node_index ASC
            LIMIT ? OFFSET ?
            """.trimIndent()
        ).use { statement ->
            statement.setString(1, conversationId)
            statement.setInt(2, limit)
            statement.setInt(3, offset)
            statement.executeQuery().use { rs ->
                val results = mutableListOf<MessageNodeEntity>()
                while (rs.next()) {
                    results.add(
                        MessageNodeEntity(
                            id = rs.getString("id"),
                            conversationId = rs.getString("conversation_id"),
                            nodeIndex = rs.getInt("node_index"),
                            messages = rs.getString("messages"),
                            selectIndex = rs.getInt("select_index")
                        )
                    )
                }
                return results
            }
        }
    }

    private fun saveMessageNodes(conn: Connection, conversationId: String, nodes: List<MessageNode>) {
        if (nodes.isEmpty()) return
        conn.prepareStatement(
            """
            INSERT INTO message_node (id, conversation_id, node_index, messages, select_index)
            VALUES (?, ?, ?, ?, ?)
            """.trimIndent()
        ).use { statement ->
            nodes.forEachIndexed { index, node ->
                statement.setString(1, node.id.toString())
                statement.setString(2, conversationId)
                statement.setInt(3, index)
                statement.setString(4, JsonInstant.encodeToString(node.messages))
                statement.setInt(5, node.selectIndex)
                statement.addBatch()
            }
            statement.executeBatch()
        }
    }

    private fun deleteMessageNodes(conn: Connection, conversationId: String) {
        conn.prepareStatement("DELETE FROM message_node WHERE conversation_id = ?").use { statement ->
            statement.setString(1, conversationId)
            statement.executeUpdate()
        }
    }
}

private fun java.sql.ResultSet.toConversationEntity(): ConversationEntity {
    val suggestions = getString("suggestions") ?: "[]"
    return ConversationEntity(
        id = getString("id"),
        assistantId = getString("assistant_id"),
        title = getString("title"),
        nodes = getString("nodes") ?: "[]",
        createAt = getLong("create_at"),
        updateAt = getLong("update_at"),
        truncateIndex = getInt("truncate_index"),
        chatSuggestions = suggestions,
        isPinned = getInt("is_pinned") != 0
    )
}

/**
 * 轻量级的会话查询结果，不包含 nodes 和 suggestions 字段
 */
data class LightConversationEntity(
    val id: String,
    val assistantId: String,
    val title: String,
    val isPinned: Boolean,
    val createAt: Long,
    val updateAt: Long,
)
