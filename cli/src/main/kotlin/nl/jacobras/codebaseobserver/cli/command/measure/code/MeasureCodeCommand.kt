package nl.jacobras.codebaseobserver.cli.command.measure.code

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import kotlinx.coroutines.runBlocking
import nl.jacobras.codebaseobserver.cli.util.GitInfoCollector
import nl.jacobras.codebaseobserver.cli.util.ServerUploader
import nl.jacobras.codebaseobserver.dto.CodeMetricsRequest
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

        val linesOfCode = countLinesOfCode(targetPath, includePatterns, excludePatterns)
        println("Counted $linesOfCode lines of code in $targetPath")

        serverUrl?.let { url ->
            val payload = CodeMetricsRequest(
                projectId = projectId,
                gitHash = GitInfoCollector.getGitHash(targetPath),
                gitDate = GitInfoCollector.getGitDate(targetPath),
                linesOfCode = linesOfCode
            )
            runBlocking {
                uploader.upload(
                    serverUrl = url,
                    endpoint = "metrics/code",
                    payload = payload
                )
            }
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

        var totalLines = 0
        var filesProcessed = 0

        Files.walk(root).use { stream ->
            stream.asSequence()
                .filter { it.isRegularFile() }
                .filter { path ->
                    (includeMatchers.isEmpty() || includeMatchers.any { it.matches(path) }) &&
                            excludeMatchers.none { it.matches(path) }
                }
                .forEach { filePath ->
                    val lines = Files.lines(filePath).use { it.count().toInt() }
                    totalLines += lines
                    filesProcessed++

                    if (filesProcessed % 1000 == 0) {
                        println("Processed $filesProcessed files...")
                    }
                }
        }

        println("Finished processing $filesProcessed files.")
        return totalLines
    }
}