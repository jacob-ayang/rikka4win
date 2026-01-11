package me.rerere.rikkahub.desktop.settings

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

data class ProviderSummary(
    val name: String,
    val modelCount: Int,
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

fun DesktopSettings.totalModelCount(): Int {
    return providerSummaries().sumOf { it.modelCount }
}
