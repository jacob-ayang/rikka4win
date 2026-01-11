package me.rerere.rikkahub.desktop.backup

import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.rerere.rikkahub.desktop.settings.S3Config
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import java.time.Instant
import javax.xml.parsers.DocumentBuilderFactory

class DesktopS3Client(
    private val config: S3Config,
    private val httpClient: HttpClient,
) {
    suspend fun putObject(
        key: String,
        data: ByteArray,
        contentType: String = "application/octet-stream",
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val path = "/${key.trimStart('/')}"
            val signed = S3SignatureV4.sign(
                config = config,
                method = "PUT",
                path = path,
                payload = data,
                contentType = contentType,
            )

            val response = httpClient.request(signed.url) {
                method = HttpMethod.Put
                headers {
                    signed.headers.forEach { (k, v) -> append(k, v) }
                }
                setBody(data)
            }

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                throw IllegalStateException("Failed to put object: ${response.status} $errorBody")
            }
            Unit
        }
    }

    suspend fun getObject(key: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        runCatching {
            val path = "/${key.trimStart('/')}"
            val signed = S3SignatureV4.sign(
                config = config,
                method = "GET",
                path = path,
            )

            val response = httpClient.request(signed.url) {
                method = HttpMethod.Get
                headers {
                    signed.headers.forEach { (k, v) -> append(k, v) }
                }
            }

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                throw IllegalStateException("Failed to get object: ${response.status} $errorBody")
            }

            response.bodyAsChannel().toInputStream().readBytes()
        }
    }

    suspend fun deleteObject(key: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val path = "/${key.trimStart('/')}"
            val signed = S3SignatureV4.sign(
                config = config,
                method = "DELETE",
                path = path,
            )

            val response = httpClient.request(signed.url) {
                method = HttpMethod.Delete
                headers {
                    signed.headers.forEach { (k, v) -> append(k, v) }
                }
            }

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                throw IllegalStateException("Failed to delete object: ${response.status} $errorBody")
            }
            Unit
        }
    }

    suspend fun listObjects(
        prefix: String = "",
        maxKeys: Int = 1000,
    ): Result<List<S3Object>> = withContext(Dispatchers.IO) {
        runCatching {
            val queryParams = mutableMapOf(
                "list-type" to "2",
                "max-keys" to maxKeys.toString(),
            )
            if (prefix.isNotEmpty()) queryParams["prefix"] = prefix

            val signed = S3SignatureV4.sign(
                config = config,
                method = "GET",
                path = "/",
                queryParams = queryParams,
            )

            val response = httpClient.request(signed.url) {
                method = HttpMethod.Get
                headers {
                    signed.headers.forEach { (k, v) -> append(k, v) }
                }
            }

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                throw IllegalStateException("Failed to list objects: ${response.status} $errorBody")
            }

            parseList(response.bodyAsText())
        }
    }

    private fun parseList(xml: String): List<S3Object> {
        val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document = builder.parse(InputSource(StringReader(xml)))
        val contents = document.getElementsByTagName("Contents")
        val results = mutableListOf<S3Object>()
        for (i in 0 until contents.length) {
            val element = contents.item(i) as? Element ?: continue
            val key = element.getElementsByTagName("Key").item(0)?.textContent ?: continue
            val size = element.getElementsByTagName("Size").item(0)?.textContent?.toLongOrNull() ?: 0L
            val lastModifiedText = element.getElementsByTagName("LastModified").item(0)?.textContent
            val lastModified = runCatching { Instant.parse(lastModifiedText) }.getOrNull()
            results.add(
                S3Object(
                    key = key,
                    size = size,
                    lastModified = lastModified
                )
            )
        }
        return results
    }
}

data class S3Object(
    val key: String,
    val size: Long,
    val lastModified: Instant?,
)
