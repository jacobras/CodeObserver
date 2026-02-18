package nl.jacobras.codebaseobserver.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
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
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.streams.asSequence

class CountFilesCommand : CliktCommand(name = "count-files") {
    private val path by option("--path", help = "Folder to scan").default(".")
    private val serverUrl by option("--server", help = "Server base URL. Without this, the count will not be uploaded.")
    private val extensions by option("--extensions", help = "Comma-separated list of extensions to filter on.")

    override fun run() {
        val targetPath = File(path).toPath().normalize().toAbsolutePath()
        val extFilter = extensions?.split(",")?.map { it.trim().lowercase() }?.filter { it.isNotEmpty() }

        val fileCount = countFiles(targetPath, extFilter)
        println("Counted $fileCount files in $targetPath")

        serverUrl?.let { url ->
            val gitHash = runCommand("git", "rev-parse", "HEAD")?.trim().orEmpty()
            val gitDate = runCommand("git", "show", "-s", "--format=%cI", "HEAD")?.trim().orEmpty()

            require(gitHash.isNotEmpty()) {
                "Could not determine git hash. Make sure you are in a git repository."
            }
            require(gitDate.isNotEmpty()) {
                "Could not determine git date. Make sure you are in a git repository."
            }
            runBlocking { upload(url, gitHash, gitDate, fileCount) }
        }
    }

    private fun countFiles(root: Path, extensions: List<String>?): Int {
        if (!Files.exists(root)) {
            return 0
        }
        Files.walk(root).use { stream ->
            return stream.asSequence()
                .filter { it.isRegularFile() }
                .count { path ->
                    if (extensions.isNullOrEmpty()) {
                        true
                    } else {
                        val name = path.fileName.toString().lowercase()
                        extensions.any { ext -> name.endsWith(ext) || name.endsWith(".$ext") }
                    }
                }
        }
    }
}

private suspend fun upload(
    serverUrl: String,
    gitHash: String,
    gitDate: String,
    fileCount: Int
) {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    val payload = CountRequest(
        gitHash = gitHash,
        gitDate = gitDate,
        fileCount = fileCount
    )
    val response = client.post("${serverUrl.trimEnd('/')}/counts") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        contentType(ContentType.Application.Json)
        setBody(payload)
    }
    client.close()
    val statusCode = response.status.value
    val responseBody = response.bodyAsText()
    println("Server response: $statusCode: $responseBody")
}