package me.rerere.rikkahub.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import me.rerere.rikkahub.data.model.AssistantMemory

class MemoryRepository {
    private data class DesktopMemory(val assistantId: String, val memory: AssistantMemory)

    private val memories = MutableStateFlow<List<DesktopMemory>>(emptyList())

    fun getMemoriesOfAssistantFlow(assistantId: String): Flow<List<AssistantMemory>> =
        memories.map { list -> list.filter { it.assistantId == assistantId }.map { it.memory } }

    suspend fun getMemoriesOfAssistant(assistantId: String): List<AssistantMemory> {
        return memories.value.filter { it.assistantId == assistantId }.map { it.memory }
    }

    suspend fun deleteMemoriesOfAssistant(assistantId: String) {
        memories.value = memories.value.filterNot { it.assistantId == assistantId }
    }

    suspend fun updateContent(id: Int, content: String): AssistantMemory {
        val updated = memories.value.map { entry ->
            if (entry.memory.id == id) entry.copy(memory = entry.memory.copy(content = content)) else entry
        }
        memories.value = updated
        return updated.first { it.memory.id == id }.memory
    }

    suspend fun addMemory(assistantId: String, content: String): AssistantMemory {
        val nextId = (memories.value.maxOfOrNull { it.memory.id } ?: 0) + 1
        val memory = AssistantMemory(id = nextId, content = content)
        memories.value = memories.value + DesktopMemory(assistantId, memory)
        return memory
    }

    suspend fun deleteMemory(id: Int) {
        memories.value = memories.value.filterNot { it.memory.id == id }
    }
}
