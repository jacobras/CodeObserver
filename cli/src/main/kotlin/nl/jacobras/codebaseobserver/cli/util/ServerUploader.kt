package nl.jacobras.codebaseobserver.cli.util

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
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

    suspend fun upload(
        serverUrl: String,
        endpoint: String,
        payload: Any
    ) {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val response = client.post("${serverUrl.trimEnd('/')}/$endpoint") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        client.close()
        val statusCode = response.status.value
        val responseBody = response.bodyAsText()
        require(response.status.isSuccess()) {
            "Error uploading: $statusCode - $responseBody"
        }
        println("Server response: $statusCode - $responseBody")
    }
}