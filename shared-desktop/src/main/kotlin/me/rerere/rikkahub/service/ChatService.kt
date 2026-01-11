package me.rerere.rikkahub.service

import android.app.Application
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.rerere.ai.core.MessageRole
import me.rerere.ai.ui.UIMessage
import me.rerere.ai.ui.UIMessagePart
import me.rerere.rikkahub.AppScope
import me.rerere.rikkahub.data.ai.mcp.McpManager
import me.rerere.rikkahub.data.datastore.SettingsStore
import me.rerere.rikkahub.data.datastore.getCurrentAssistant
import me.rerere.rikkahub.data.model.Conversation
import me.rerere.rikkahub.data.model.MessageNode
import me.rerere.rikkahub.data.model.toMessageNode
import me.rerere.rikkahub.data.repository.ConversationRepository
import me.rerere.rikkahub.data.repository.MemoryRepository
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid

data class ChatError(
    val id: Uuid = Uuid.random(),
    val error: Throwable,
    val timestamp: Long = System.currentTimeMillis()
)

class ChatService(
    private val context: Application,
    private val appScope: AppScope,
    private val settingsStore: SettingsStore,
    private val conversationRepo: ConversationRepository,
    private val memoryRepository: MemoryRepository,
    val mcpManager: McpManager,
) {
    private val conversations = ConcurrentHashMap<Uuid, MutableStateFlow<Conversation>>()
    private val conversationJobs = ConcurrentHashMap<Uuid, MutableStateFlow<Job?>>()
    private val _errors = MutableStateFlow<List<ChatError>>(emptyList())
    val errors: StateFlow<List<ChatError>> = _errors.asStateFlow()
    private val _generationDoneFlow = MutableSharedFlow<Uuid>()
    val generationDoneFlow: SharedFlow<Uuid> = _generationDoneFlow

    fun addConversationReference(conversationId: Uuid) {
        conversations.putIfAbsent(conversationId, MutableStateFlow(newConversation(conversationId)))
        conversationJobs.putIfAbsent(conversationId, MutableStateFlow(null))
    }

    fun removeConversationReference(conversationId: Uuid) {
        conversations.remove(conversationId)
        conversationJobs.remove(conversationId)
    }

    fun getConversationFlow(conversationId: Uuid): StateFlow<Conversation> {
        return conversations.getOrPut(conversationId) { MutableStateFlow(newConversation(conversationId)) }
    }

    fun getGenerationJobStateFlow(conversationId: Uuid): StateFlow<Job?> {
        return conversationJobs.getOrPut(conversationId) { MutableStateFlow(null) }
    }

    fun getConversationJobs(): Flow<Map<Uuid, Job?>> = flow {
        emit(conversationJobs.mapValues { it.value.value })
    }

    fun initializeConversation(conversationId: Uuid) {
        conversations[conversationId]?.value = newConversation(conversationId, newConversation = true)
    }

    fun sendMessage(conversationId: Uuid, content: List<UIMessagePart>, answer: Boolean = true) {
        val flow = conversations.getOrPut(conversationId) { MutableStateFlow(newConversation(conversationId)) }
        val userMessage = UIMessage(role = MessageRole.USER, parts = content)
        val updated = appendMessage(flow.value, userMessage)
        val finalConversation = if (answer) {
            val assistantMessage = UIMessage(
                role = MessageRole.ASSISTANT,
                parts = listOf(UIMessagePart.Text("..."))
            )
            appendMessage(updated, assistantMessage)
        } else {
            updated
        }
        flow.value = finalConversation.copy(updateAt = Instant.now())
        _generationDoneFlow.tryEmit(conversationId)
    }

    fun saveConversation(conversationId: Uuid, conversation: Conversation) {
        conversations.getOrPut(conversationId) { MutableStateFlow(conversation) }.value = conversation
    }

    fun regenerateAtMessage(conversationId: Uuid, message: UIMessage, regenerateAssistantMsg: Boolean = true) {
        // No-op for desktop shim.
    }

    fun translateMessage(conversationId: Uuid, message: UIMessage, targetLanguage: java.util.Locale) {
        // No-op for desktop shim.
    }

    fun generateTitle(conversationId: Uuid, conversation: Conversation, force: Boolean = false) {
        // No-op for desktop shim.
    }

    fun generateSuggestion(conversationId: Uuid, conversation: Conversation) {
        // No-op for desktop shim.
    }

    fun clearTranslationField(conversationId: Uuid, messageId: Uuid) {
        // No-op for desktop shim.
    }

    fun dismissError(id: Uuid) {
        _errors.value = _errors.value.filterNot { it.id == id }
    }

    fun clearAllErrors() {
        _errors.value = emptyList()
    }

    private fun newConversation(conversationId: Uuid, newConversation: Boolean = false): Conversation {
        val assistantId = settingsStore.settingsFlow.value.getCurrentAssistant().id
        return Conversation.ofId(id = conversationId, assistantId = assistantId, newConversation = newConversation)
    }

    private fun appendMessage(conversation: Conversation, message: UIMessage): Conversation {
        val node = message.toMessageNode()
        val nodes = conversation.messageNodes + node
        return conversation.copy(messageNodes = nodes, updateAt = Instant.now())
    }
}
