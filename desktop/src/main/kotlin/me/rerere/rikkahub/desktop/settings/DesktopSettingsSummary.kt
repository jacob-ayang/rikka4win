package me.rerere.rikkahub.desktop.settings

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

data class ProviderSummary(
    val name: String,
    val modelCount: Int,
)

data class ProviderDetail(
    val name: String,
    val type: String,
    val baseUrl: String?,
    val enabled: Boolean,
    val modelNames: List<String>,
)

fun DesktopSettings.providerSummaries(): List<ProviderSummary> {
    val providersArray = providers as? JsonArray ?: return emptyList()
    return providersArray.mapNotNull { element ->
        val obj = element as? JsonObject ?: return@mapNotNull null
        val name = obj["name"]?.jsonPrimitive?.contentOrNull ?: "Provider"
        val models = obj["models"] as? JsonArray
        ProviderSummary(name = name, modelCount = models?.size ?: 0)
    }
}

fun DesktopSettings.providerDetails(): List<ProviderDetail> {
    val providersArray = providers as? JsonArray ?: return emptyList()
    return providersArray.mapNotNull { element ->
        val obj = element as? JsonObject ?: return@mapNotNull null
        val name = obj["name"]?.jsonPrimitive?.contentOrNull ?: "Provider"
        val type = obj["type"]?.jsonPrimitive?.contentOrNull
            ?: obj["class"]?.jsonPrimitive?.contentOrNull
            ?: "unknown"
        val baseUrl = obj["baseUrl"]?.jsonPrimitive?.contentOrNull
        val enabled = obj["enabled"]?.jsonPrimitive?.contentOrNull?.toBooleanStrictOrNull() ?: true
        val modelsArray = obj["models"] as? JsonArray
        val modelNames = modelsArray?.mapNotNull { modelElement ->
            val modelObj = modelElement as? JsonObject ?: return@mapNotNull null
            modelObj["name"]?.jsonPrimitive?.contentOrNull
                ?: modelObj["id"]?.jsonPrimitive?.contentOrNull
        } ?: emptyList()
        ProviderDetail(
            name = name,
            type = type,
            baseUrl = baseUrl,
            enabled = enabled,
            modelNames = modelNames,
        )
    }
}

fun DesktopSettings.totalModelCount(): Int {
    return providerSummaries().sumOf { it.modelCount }
}
