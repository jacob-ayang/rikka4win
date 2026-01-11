package me.rerere.ai.provider

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CustomHeader(
    val name: String = "",
    val value: String = "",
)

@Serializable
data class CustomBody(
    val key: String = "",
    val value: JsonElement? = null,
)
