package me.rerere.rikkahub.data.ai.mcp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.uuid.Uuid

sealed class McpStatus {
    data object Idle : McpStatus()
    data object Connecting : McpStatus()
    data object Connected : McpStatus()
    data class Error(val message: String) : McpStatus()
}

class McpManager {
    val syncingStatus = MutableStateFlow<Map<Uuid, McpStatus>>(emptyMap())

    fun getStatus(config: McpServerConfig): Flow<McpStatus> {
        return syncingStatus.map { it[config.id] ?: McpStatus.Idle }
    }

    fun getClient(config: McpServerConfig): Any? = null

    fun getAllAvailableTools(): List<McpTool> = emptyList()

    fun callTool(toolName: String, args: JsonObject): JsonElement {
        return JsonPrimitive("Tool call not supported on desktop")
    }

    fun syncAll() = Unit
}
