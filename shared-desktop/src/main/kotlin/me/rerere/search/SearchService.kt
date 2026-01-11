package me.rerere.search

import androidx.compose.runtime.Composable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import me.rerere.ai.core.InputSchema
import kotlin.uuid.Uuid

interface SearchService<T : SearchServiceOptions> {
    val name: String
    val parameters: InputSchema?
    val scrapingParameters: InputSchema?

    @Composable
    fun Description()

    suspend fun search(
        params: JsonObject,
        commonOptions: SearchCommonOptions,
        serviceOptions: T
    ): Result<SearchResult>

    suspend fun scrape(
        params: JsonObject,
        commonOptions: SearchCommonOptions,
        serviceOptions: T
    ): Result<ScrapedResult>

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <T : SearchServiceOptions> getService(options: T): SearchService<T> {
            return object : SearchService<T> {
                override val name: String = "Search"
                override val parameters: InputSchema? = null
                override val scrapingParameters: InputSchema? = null

                @Composable
                override fun Description() = Unit

                override suspend fun search(
                    params: JsonObject,
                    commonOptions: SearchCommonOptions,
                    serviceOptions: T
                ): Result<SearchResult> = Result.success(SearchResult(items = emptyList()))

                override suspend fun scrape(
                    params: JsonObject,
                    commonOptions: SearchCommonOptions,
                    serviceOptions: T
                ): Result<ScrapedResult> = Result.success(ScrapedResult(urls = emptyList()))
            }
        }
    }
}

@Serializable
data class SearchCommonOptions(
    val resultSize: Int = 10,
)

@Serializable
data class SearchResult(
    val answer: String? = null,
    val items: List<SearchResultItem>,
) {
    @Serializable
    data class SearchResultItem(
        val title: String,
        val url: String,
        val text: String,
    )
}

@Serializable
data class ScrapedResult(
    val urls: List<ScrapedResultUrl>,
)

@Serializable
data class ScrapedResultUrl(
    val url: String,
    val content: String,
    val metadata: ScrapedResultMetadata? = null,
)

@Serializable
data class ScrapedResultMetadata(
    val title: String? = null,
    val description: String? = null,
    val language: String? = null,
)

@Serializable
sealed class SearchServiceOptions {
    abstract val id: Uuid

    companion object {
        val DEFAULT = BingLocalOptions()

        val TYPES = mapOf(
            BingLocalOptions::class to "Bing",
            ZhipuOptions::class to "智谱",
            TavilyOptions::class to "Tavily",
            ExaOptions::class to "Exa",
            SearXNGOptions::class to "SearXNG",
            LinkUpOptions::class to "LinkUp",
            BraveOptions::class to "Brave",
            MetasoOptions::class to "秘塔",
            OllamaOptions::class to "Ollama",
            PerplexityOptions::class to "Perplexity",
            FirecrawlOptions::class to "Firecrawl",
            JinaOptions::class to "Jina",
            BochaOptions::class to "博查",
        )
    }

    @Serializable
    @SerialName("bing_local")
    class BingLocalOptions(
        override val id: Uuid = Uuid.random()
    ) : SearchServiceOptions()

    @Serializable
    @SerialName("zhipu")
    data class ZhipuOptions(
        override val id: Uuid = Uuid.random(),
        val apiKey: String = "",
    ) : SearchServiceOptions()

    @Serializable
    @SerialName("tavily")
    data class TavilyOptions(
        override val id: Uuid = Uuid.random(),
        val apiKey: String = "",
        val depth: String = "advanced",
    ) : SearchServiceOptions()

    @Serializable
    @SerialName("exa")
    data class ExaOptions(
        override val id: Uuid = Uuid.random(),
        val apiKey: String = "",
    ) : SearchServiceOptions()

    @Serializable
    @SerialName("searxng")
    data class SearXNGOptions(
        override val id: Uuid = Uuid.random(),
        val url: String = "",
        val engines: String = "",
        val language: String = "",
        val username: String = "",
        val password: String = "",
    ) : SearchServiceOptions()

    @Serializable
    @SerialName("linkup")
    data class LinkUpOptions(
        override val id: Uuid = Uuid.random(),
        val apiKey: String = "",
        val depth: String = "standard",
    ) : SearchServiceOptions()

    @Serializable
    @SerialName("brave")
    data class BraveOptions(
        override val id: Uuid = Uuid.random(),
        val apiKey: String = "",
    ) : SearchServiceOptions()

    @Serializable
    @SerialName("metaso")
    data class MetasoOptions(
        override val id: Uuid = Uuid.random(),
        val apiKey: String = "",
    ) : SearchServiceOptions()

    @Serializable
    @SerialName("ollama")
    data class OllamaOptions(
        override val id: Uuid = Uuid.random(),
        val apiKey: String = "",
    ) : SearchServiceOptions()

    @Serializable
    @SerialName("perplexity")
    data class PerplexityOptions(
        override val id: Uuid = Uuid.random(),
        val apiKey: String = "",
        val maxTokensPerPage: Int? = 1024,
    ) : SearchServiceOptions()

    @Serializable
    @SerialName("firecrawl")
    data class FirecrawlOptions(
        override val id: Uuid = Uuid.random(),
        val apiKey: String = "",
    ) : SearchServiceOptions()

    @Serializable
    @SerialName("jina")
    data class JinaOptions(
        override val id: Uuid = Uuid.random(),
        val apiKey: String = "",
    ) : SearchServiceOptions()

    @Serializable
    @SerialName("bocha")
    data class BochaOptions(
        override val id: Uuid = Uuid.random(),
        val apiKey: String = "",
        val summary: Boolean = true,
    ) : SearchServiceOptions()
}
