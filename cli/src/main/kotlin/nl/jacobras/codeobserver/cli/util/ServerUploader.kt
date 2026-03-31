package nl.jacobras.codeobserver.cli.util

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal class ServerUploader {

    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun upload(
        serverUrl: String,
        endpoint: String,
        payload: Any
    ) {
        val response = client.post("${serverUrl.trimEnd('/')}/$endpoint") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        val statusCode = response.status.value
        val responseBody = response.bodyAsText()
        require(response.status.isSuccess()) {
            "Error uploading: $statusCode - $responseBody"
        }
        println("Server response: $statusCode - $responseBody")
    }

    suspend inline fun <reified T> fetch(
        serverUrl: String,
        endpoint: String
    ): T {
        val response = client.get("${serverUrl.trimEnd('/')}/$endpoint")
        val statusCode = response.status.value
        val responseBody = response.bodyAsText()
        require(response.status.isSuccess()) {
            "Error fetching: $statusCode - $responseBody"
        }
        return json.decodeFromString(responseBody)
    }
}