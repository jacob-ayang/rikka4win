package me.rerere.rikkahub.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.Serializable

class UpdateChecker {
    fun checkUpdate(): Flow<UiState<UpdateInfo>> = flowOf(UiState.Success(UpdateInfo()))

    fun downloadUpdate(context: android.content.Context, download: UpdateDownload) = Unit
}

@Serializable
data class UpdateDownload(
    val name: String = "",
    val url: String = "",
    val size: String = "",
)

@Serializable
data class UpdateInfo(
    val version: String = "",
    val publishedAt: String = "",
    val changelog: String = "",
    val downloads: List<UpdateDownload> = emptyList(),
)

@JvmInline
value class Version(val value: String) : Comparable<Version> {
    private fun parseVersion(): List<Int> {
        return value.split(".").map { it.toIntOrNull() ?: 0 }
    }

    override fun compareTo(other: Version): Int {
        val thisParts = parseVersion()
        val otherParts = other.parseVersion()
        val maxLength = maxOf(thisParts.size, otherParts.size)
        for (i in 0 until maxLength) {
            val thisPart = thisParts.getOrElse(i) { 0 }
            val otherPart = otherParts.getOrElse(i) { 0 }
            if (thisPart != otherPart) return thisPart.compareTo(otherPart)
        }
        return 0
    }
}

operator fun String.compareTo(other: Version): Int = Version(this).compareTo(other)
operator fun Version.compareTo(other: String): Int = this.compareTo(Version(other))
