package nl.jacobras.codebaseobserver.cli.command.measure.code

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import kotlinx.coroutines.runBlocking
import nl.jacobras.codebaseobserver.cli.util.GitInfoCollector
import nl.jacobras.codebaseobserver.cli.util.ServerUploader
import nl.jacobras.codebaseobserver.dto.CodeMetricsRequest
import nl.jacobras.codebaseobserver.dto.MigrationDto
import nl.jacobras.codebaseobserver.dto.MigrationProgressRequest
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.streams.asSequence

class MeasureCodeCommand internal constructor(
    private val uploader: ServerUploader
) : CliktCommand(name = "measure-code") {
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
        println("Going to measure code")
        val targetPath = File(path).toPath().normalize().toAbsolutePath()
        val includePatterns = include.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val excludePatterns = exclude.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        val (linesOfCode, importCounts) = scanFiles(targetPath, includePatterns, excludePatterns)
        println("Counted $linesOfCode lines of code in $targetPath")

        serverUrl?.let { url ->
            val gitHash = GitInfoCollector.getGitHash(targetPath)
            val gitDate = GitInfoCollector.getGitDate(targetPath)
            val payload = CodeMetricsRequest(
                projectId = projectId,
                gitHash = gitHash,
                gitDate = gitDate,
                linesOfCode = linesOfCode
            )
            runBlocking {
                uploader.upload(
                    serverUrl = url,
                    endpoint = "metrics/code",
                    payload = payload
                )
                val migrations = uploader.fetch<List<MigrationDto>>(
                    serverUrl = url,
                    endpoint = "migrations?projectId=$projectId"
                )
                migrations
                    .filter { it.type == "importUsage" }
                    .forEach { migration ->
                        uploader.upload(
                            serverUrl = url,
                            endpoint = "migrationProgress",
                            payload = MigrationProgressRequest(
                                migrationId = migration.id,
                                gitHash = gitHash,
                                gitDate = gitDate,
                                count = importCounts[migration.rule] ?: 0
                            )
                        )
                    }
            }
        }
    }

    private fun scanFiles(
        root: Path,
        includePatterns: List<String>,
        excludePatterns: List<String>
    ): ScanResult {
        if (!Files.exists(root)) return ScanResult(0, emptyMap())

        val includeMatchers = includePatterns.map { FileSystems.getDefault().getPathMatcher("glob:$it") }
        val excludeMatchers = excludePatterns.map { FileSystems.getDefault().getPathMatcher("glob:$it") }

        var totalLines = 0
        var filesProcessed = 0
        val importCounts = mutableMapOf<String, Int>()

        Files.walk(root).use { stream ->
            stream.asSequence()
                .filter { it.isRegularFile() }
                .filter { path ->
                    (includeMatchers.isEmpty() || includeMatchers.any { it.matches(path) }) &&
                            excludeMatchers.none { it.matches(path) }
                }
                .forEach { filePath ->
                    Files.lines(filePath).use { lines ->
                        lines.forEach { line ->
                            totalLines++
                            val importPath = ImportCounter.extractImportPath(line.trim()) ?: return@forEach
                            importCounts[importPath] = (importCounts[importPath] ?: 0) + 1
                        }
                    }
                    filesProcessed++

                    if (filesProcessed % 1_000 == 0) {
                        println("Processed $filesProcessed files...")
                    }
                }
        }

        println("Finished processing $filesProcessed files.")
        return ScanResult(totalLines, importCounts)
    }
}

private data class ScanResult(val linesOfCode: Int, val importCounts: Map<String, Int>)