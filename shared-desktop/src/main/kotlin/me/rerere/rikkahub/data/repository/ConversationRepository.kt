package me.rerere.rikkahub.data.repository

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import me.rerere.rikkahub.data.model.Conversation
import me.rerere.rikkahub.utils.deleteChatFiles
import kotlin.uuid.Uuid

class ConversationRepository(
    private val context: Context,
) {
    private val conversations = MutableStateFlow<List<Conversation>>(emptyList())

    suspend fun getRecentConversations(assistantId: Uuid, limit: Int = 10): List<Conversation> {
        return conversations.value.filter { it.assistantId == assistantId }.take(limit)
    }

    fun getConversationsOfAssistant(assistantId: Uuid): Flow<List<Conversation>> {
        return conversations.map { list ->
            list.filter { it.assistantId == assistantId }
        }
    }

    fun getConversationsOfAssistantPaging(assistantId: Uuid): Flow<PagingData<Conversation>> =
        Pager(
            config = PagingConfig(pageSize = 20, initialLoadSize = 40, enablePlaceholders = false),
            pagingSourceFactory = { ListPagingSource(conversations.value.filter { it.assistantId == assistantId }) }
        ).flow

    fun searchConversations(titleKeyword: String): Flow<List<Conversation>> {
        return conversations.map { list ->
            list.filter { it.title.contains(titleKeyword, ignoreCase = true) }
        }
    }

    fun searchConversationsPaging(titleKeyword: String): Flow<PagingData<Conversation>> =
        Pager(
            config = PagingConfig(pageSize = 20, initialLoadSize = 40, enablePlaceholders = false),
            pagingSourceFactory = {
                ListPagingSource(
                    conversations.value.filter { it.title.contains(titleKeyword, ignoreCase = true) }
                )
            }
        ).flow

    fun searchConversationsOfAssistant(assistantId: Uuid, titleKeyword: String): Flow<List<Conversation>> {
        return conversations.map { list ->
            list.filter { it.assistantId == assistantId && it.title.contains(titleKeyword, ignoreCase = true) }
        }
    }

    fun searchConversationsOfAssistantPaging(assistantId: Uuid, titleKeyword: String): Flow<PagingData<Conversation>> =
        Pager(
            config = PagingConfig(pageSize = 20, initialLoadSize = 40, enablePlaceholders = false),
            pagingSourceFactory = {
                ListPagingSource(
                    conversations.value.filter {
                        it.assistantId == assistantId && it.title.contains(titleKeyword, ignoreCase = true)
                    }
                )
            }
        ).flow

    suspend fun getConversationById(uuid: Uuid): Conversation? {
        return conversations.value.find { it.id == uuid }
    }

    suspend fun insertConversation(conversation: Conversation) {
        conversations.value = conversations.value + conversation
    }

    suspend fun updateConversation(conversation: Conversation) {
        conversations.value = conversations.value.map { if (it.id == conversation.id) conversation else it }
    }

    suspend fun deleteConversation(conversation: Conversation) {
        conversations.value = conversations.value.filterNot { it.id == conversation.id }
        context.deleteChatFiles(conversation.files)
    }

    suspend fun deleteConversationOfAssistant(assistantId: Uuid) {
        conversations.value.filter { it.assistantId == assistantId }.forEach { deleteConversation(it) }
    }

    fun getPinnedConversations(): Flow<List<Conversation>> {
        return conversations.map { list -> list.filter { it.isPinned } }
    }

    suspend fun togglePinStatus(conversationId: Uuid) {
        conversations.value = conversations.value.map { conversation ->
            if (conversation.id == conversationId) conversation.copy(isPinned = !conversation.isPinned) else conversation
        }
    }
}

private class ListPagingSource<T : Any>(private val items: List<T>) : PagingSource<Int, T>() {
    override fun loadAll(): List<T> = items
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
