package me.rerere.rikkahub.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.rerere.rikkahub.data.db.DesktopDatabase
import me.rerere.rikkahub.data.db.entity.MemoryEntity
import me.rerere.rikkahub.data.model.AssistantMemory

class MemoryRepository(
    private val database: DesktopDatabase,
) {
    private val memories = MutableStateFlow(loadAllMemories())

    fun getMemoriesOfAssistantFlow(assistantId: String): Flow<List<AssistantMemory>> =
        memories.map { list ->
            list.filter { it.assistantId == assistantId }
                .map { AssistantMemory(id = it.id, content = it.content) }
        }

    suspend fun getMemoriesOfAssistant(assistantId: String): List<AssistantMemory> {
        return memories.value
            .filter { it.assistantId == assistantId }
            .map { AssistantMemory(id = it.id, content = it.content) }
    }

    suspend fun deleteMemoriesOfAssistant(assistantId: String) {
        withContext(Dispatchers.IO) {
            database.query { conn ->
                conn.prepareStatement("DELETE FROM memoryentity WHERE assistant_id = ?").use { statement ->
                    statement.setString(1, assistantId)
                    statement.executeUpdate()
                }
            }
            refreshMemories()
        }
    }

    suspend fun updateContent(id: Int, content: String): AssistantMemory {
        return withContext(Dispatchers.IO) {
            database.query { conn ->
                conn.prepareStatement("UPDATE memoryentity SET content = ? WHERE id = ?").use { statement ->
                    statement.setString(1, content)
                    statement.setInt(2, id)
                    statement.executeUpdate()
                }
            }
            refreshMemories()
            AssistantMemory(id = id, content = content)
        }
    }

    suspend fun addMemory(assistantId: String, content: String): AssistantMemory {
        return withContext(Dispatchers.IO) {
            val id = database.query { conn ->
                conn.prepareStatement(
                    "INSERT INTO memoryentity (assistant_id, content) VALUES (?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS
                ).use { statement ->
                    statement.setString(1, assistantId)
                    statement.setString(2, content)
                    statement.executeUpdate()
                    statement.generatedKeys.use { rs ->
                        if (rs.next()) rs.getInt(1) else 0
                    }
                }
            }
            refreshMemories()
            AssistantMemory(id = id, content = content)
        }
    }

    suspend fun deleteMemory(id: Int) {
        withContext(Dispatchers.IO) {
            database.query { conn ->
                conn.prepareStatement("DELETE FROM memoryentity WHERE id = ?").use { statement ->
                    statement.setInt(1, id)
                    statement.executeUpdate()
                }
            }
            refreshMemories()
        }
    }

    private fun refreshMemories() {
        memories.value = loadAllMemories()
    }

    private fun loadAllMemories(): List<MemoryEntity> {
        return database.query { conn ->
            conn.prepareStatement(
                "SELECT id, assistant_id, content FROM memoryentity ORDER BY id ASC"
            ).use { statement ->
                statement.executeQuery().use { rs ->
                    val results = mutableListOf<MemoryEntity>()
                    while (rs.next()) {
                        results.add(
                            MemoryEntity(
                                id = rs.getInt("id"),
                                assistantId = rs.getString("assistant_id"),
                                content = rs.getString("content") ?: ""
                            )
                        )
                    }
                    results
                }
            }
        }
    }
}
