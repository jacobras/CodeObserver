package nl.jacobras.codeobserver.cli.command.measure.code

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import kotlinx.coroutines.runBlocking
import nl.jacobras.codeobserver.cli.util.GitInfoCollector
import nl.jacobras.codeobserver.cli.util.ServerUploader
import nl.jacobras.codeobserver.dto.CodeMetricsDto
import nl.jacobras.codeobserver.dto.CodeMetricsRequest
import nl.jacobras.codeobserver.dto.MigrationDto
import nl.jacobras.codeobserver.dto.MigrationProgressRequest
import nl.jacobras.codeobserver.dto.ProjectId
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.streams.asSequence

class MeasureCodeCommand internal constructor(
    private val uploader: ServerUploader
) : CliktCommand(name = "measure-code") {
    private val serverUrl by option(
        "--server",
        help = "Server base URL. Without this, the count will not be uploaded."
    )
    private val projectId by option(
        "--project",
        help = "Project identifier for this measurement."
    ).required()
    private val path by option(
        "--path",
        help = "Folder to scan. Defaults to the current working directory."
    ).default(".")
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

        var lastKnownLines: Int? = null
        var migrations: List<MigrationDto> = emptyList()

        serverUrl?.let { url ->
            runBlocking {
                try {
                    val metrics = uploader.fetch<List<CodeMetricsDto>>(
                        serverUrl = url,
                        endpoint = "metrics?projectId=$projectId"
                    )
                    lastKnownLines = metrics
                        .filter { it.linesOfCode > 0 }
                        .maxByOrNull { it.gitDate }?.linesOfCode
                } catch (_: Exception) {
                }
                migrations = uploader.fetch<List<MigrationDto>>(
                    serverUrl = url,
                    endpoint = "migrations?projectId=$projectId"
                )
            }
        }

        val importRules = migrations.filter { it.type == "importUsage" }.map { it.rule }
        val (linesOfCode, importCounts) = scanFiles(
            root = targetPath,
            includePatterns = includePatterns,
            excludePatterns = excludePatterns,
            importRules = importRules,
            lastKnownLines = lastKnownLines
        )
        println("Counted $linesOfCode lines of code in $targetPath")

        serverUrl?.let { url ->
            val gitHash = GitInfoCollector.getGitHash(targetPath)
            val gitDate = GitInfoCollector.getGitDate(targetPath)
            runBlocking {
                uploader.upload(
                    serverUrl = url,
                    endpoint = "metrics/code",
                    payload = CodeMetricsRequest(
                        projectId = ProjectId(projectId),
                        gitHash = gitHash,
                        gitDate = gitDate,
                        linesOfCode = linesOfCode
                    )
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
        excludePatterns: List<String>,
        importRules: List<String>,
        lastKnownLines: Int? = null
    ): ScanResult {
        if (!Files.exists(root)) return ScanResult(0, emptyMap())

        val includeMatchers = includePatterns.map { FileSystems.getDefault().getPathMatcher("glob:$it") }
        val excludeMatchers = excludePatterns.map { FileSystems.getDefault().getPathMatcher("glob:$it") }

        var totalLines = 0
        var filesProcessed = 0
        val importCounts = importRules.associateWith { 0 }.toMutableMap()

        Files.walk(root).use { stream ->
            stream.asSequence()
                .filter { it.isRegularFile() }
                .filter { path ->
                    (includeMatchers.isEmpty() || includeMatchers.any { it.matches(path) }) &&
                        excludeMatchers.none { it.matches(path) }
                }
                .forEach { filePath ->
                    val text = Files.readString(filePath)
                    totalLines += text.lines().size
                    if (importRules.isNotEmpty()) {
                        ImportCounter.count(text, importRules).forEach { (rule, count) ->
                            importCounts[rule] = importCounts[rule]!! + count
                        }
                    }
                    filesProcessed++

                    if (filesProcessed % UPDATE_INTERVAL == 0) {
                        val progressSuffix = if (lastKnownLines != null && lastKnownLines > 0) {
                            val pct = (totalLines * PERCENTAGE / lastKnownLines).coerceAtMost(MAX_PERCENTAGE)
                            " (~$pct%)"
                        } else {
                            ""
                        }
                        println("Processed $filesProcessed files...$progressSuffix")
                    }
                }
        }

        println("Finished processing $filesProcessed files.")
        return ScanResult(totalLines, importCounts)
    }
}

private data class ScanResult(val linesOfCode: Int, val importCounts: Map<String, Int>)

private const val UPDATE_INTERVAL = 1000
private const val MAX_PERCENTAGE = 99
private const val PERCENTAGE = 100