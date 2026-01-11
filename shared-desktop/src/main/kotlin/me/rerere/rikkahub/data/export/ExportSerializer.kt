package me.rerere.rikkahub.data.export

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

@Serializable
data class ExportData(
    val version: Int = 1,
    val type: String,
    val data: JsonElement
)

interface ExportSerializer<T> {
    val type: String

    fun export(data: T): ExportData

    fun getExportFileName(data: T): String = "${type}.json"

    fun exportToJson(data: T, json: Json = DefaultJson): String {
        return json.encodeToString(ExportData.serializer(), export(data))
    }

    fun importFromJson(json: String): Result<T> {
        return Result.failure(UnsupportedOperationException("Import not supported on desktop yet."))
    }

    companion object {
        val DefaultJson = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            prettyPrint = false
        }
    }
}
