package nl.jacobras.codeobserver.cli.command.measure.artifact

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import kotlinx.coroutines.runBlocking
import nl.jacobras.codeobserver.cli.util.ServerUploader
import nl.jacobras.codeobserver.dto.ArtifactSizeRequest
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.humanreadable.HumanReadable
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Paths
import java.util.zip.ZipFile

class MeasureArtifactCommand internal constructor(
    private val uploader: ServerUploader
) : CliktCommand(name = "measure-artifact-size") {
    private val serverUrl by option(
        "--server",
        help = "Server base URL. Without this, the count will not be uploaded."
    ).required()
    private val projectId by option(
        "--project",
        help = "Project identifier for this measurement."
    ).required()
    private val file by option(
        "--file",
        help = "File to measure."
    ).required()
    private val name by option(
        "--name",
        help = "Name identifying this artifact."
    ).required()
    private val semVer by option(
        "--semVer",
        help = "SemVer identifying this artifact."
    ).required()

    override fun run() {
        println("Going to measure artifact size")
        val artifact = File(file)
        val size = measureSize(artifact)

        serverUrl?.let { url ->
            val payload = ArtifactSizeRequest(
                projectId = ProjectId(projectId),
                name = name,
                semVer = semVer,
                size = size
            )
            runBlocking {
                uploader.upload(
                    serverUrl = url,
                    endpoint = "artifactSizes",
                    payload = payload
                )
            }
        }
    }

    private fun measureSize(file: File): Long {
        require(file.exists()) { "File does not exist: ${file.absolutePath}" }

        return if (file.extension == "aab") {
            val fullSize = file.length()
            val adjustedSize = calculateZipSize(
                file = file,
                excludePatterns = listOf(
                    "base/lib/x86_64/**",
                    "base/lib/x86/**",
                    "base/lib/armeabi-v7a/**",
                )
            )
            val full = HumanReadable.fileSize(fullSize)
            val adjusted = HumanReadable.fileSize(adjustedSize)
            println("Full size: $full, after filtering: $adjusted")
            adjustedSize
        } else {
            val fullSize = file.length()
            println("Full size: ${HumanReadable.fileSize(fullSize)}")
            fullSize
        }
    }

    private fun calculateZipSize(file: File, excludePatterns: List<String>): Long {
        var totalSize = 0L

        val excludeMatchers = excludePatterns.map { pattern ->
            FileSystems.getDefault().getPathMatcher("glob:$pattern")
        }

        ZipFile(file).use { zip ->
            zip.entries().asSequence()
                .filter { entry ->
                    excludeMatchers.none {
                        val path = Paths.get(entry.name)
                        it.matches(path)
                    }
                }
                .forEach { entry ->
                    totalSize += entry.compressedSize
                }
        }
        return totalSize
    }
}