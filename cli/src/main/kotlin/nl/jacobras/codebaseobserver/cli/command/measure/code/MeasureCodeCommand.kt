package nl.jacobras.codebaseobserver.cli.command.measure.code

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
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
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.streams.asSequence

class MeasureCodeCommand : CliktCommand(name = "measure-code") {
    private val path by option(
        "--path",
        help = "Folder to scan. Defaults to the current working directory."
    ).default(".")
    private val serverUrl by option(
        "--server",
        help = "Server base URL. Without this, the count will not be uploaded."
    )
    private val projectId by option(
        "--project",
        help = "Project identifier for this measurement."
    ).required()
    private val include by option(
        "--include",
        help = "Glob patterns to include files/folders (comma-separated). Defaults to .kt/.kts."
    ).default("**/*.kt,**/*.kts")
    private val exclude by option(
        "--exclude",
        help = "Glob patterns to exclude files/folders (comma-separated). Defaults to common build/IDE folders.'."
    ).default("**/build/**,**/.git/**,/**/.gradle/**,**/.kotlin/**,**/.idea/**")

    override fun run() {
        val targetPath = File(path).toPath().normalize().toAbsolutePath()
        val includePatterns = include.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val excludePatterns = exclude.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        val linesOfCode = countLinesOfCode(targetPath, includePatterns, excludePatterns)
        println("Counted $linesOfCode lines of code in $targetPath")

        serverUrl?.let { url ->
            val workingDir = targetPath.toFile()
            val gitHash = runCommand(workingDir, "git", "rev-parse", "HEAD")?.trim().orEmpty()
            val gitDate = runCommand(workingDir, "git", "show", "-s", "--format=%cI", "HEAD")?.trim().orEmpty()

            require(gitHash.isNotEmpty()) {
                "Could not determine git hash. Make sure you are in a git repository."
            }
            require(gitDate.isNotEmpty()) {
                "Could not determine git date. Make sure you are in a git repository."
            }
            runBlocking { upload(url, projectId, gitHash, gitDate, linesOfCode) }
        }
    }

    private fun countLinesOfCode(
        root: Path,
        includePatterns: List<String>,
        excludePatterns: List<String>
    ): Int {
        if (!Files.exists(root)) return 0

        val includeMatchers = includePatterns.map { pattern ->
            FileSystems.getDefault().getPathMatcher("glob:$pattern")
        }
        val excludeMatchers = excludePatterns.map { pattern ->
            FileSystems.getDefault().getPathMatcher("glob:$pattern")
        }

        Files.walk(root).use { stream ->
            return stream.asSequence()
                .filter {
                    // Filter out symlinks and such.
                    it.isRegularFile()
                }
                .filter { path ->
                    // Handle inclusion/exclusion patterns.
                    (includeMatchers.isEmpty() || includeMatchers.any { it.matches(path) }) &&
                            excludeMatchers.none { it.matches(path) }
                }
                .sumOf { filePath ->
                    // Count lines in each matching file
                    Files.lines(filePath).use { lines -> lines.count().toInt() }
                }
        }
    }

    private suspend fun upload(
        serverUrl: String,
        projectId: String,
        gitHash: String,
        gitDate: String,
        linesOfCode: Int
    ) {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val payload = CountRequest(
            projectId = projectId,
            gitHash = gitHash,
            gitDate = gitDate,
            linesOfCode = linesOfCode
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
}