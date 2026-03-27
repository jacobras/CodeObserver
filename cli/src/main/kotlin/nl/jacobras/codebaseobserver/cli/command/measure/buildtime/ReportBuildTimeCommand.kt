package nl.jacobras.codebaseobserver.cli.command.measure.buildtime

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.coroutines.runBlocking
import nl.jacobras.codebaseobserver.cli.util.GitInfoCollector
import nl.jacobras.codebaseobserver.cli.util.ServerUploader
import nl.jacobras.codebaseobserver.dto.BuildTimeRequest
import nl.jacobras.codebaseobserver.dto.ProjectId
import nl.jacobras.humanreadable.HumanReadable
import java.io.File
import kotlin.time.Duration.Companion.seconds

class ReportBuildTimeCommand internal constructor(
    private val uploader: ServerUploader
) : CliktCommand(name = "report-build-time") {
    private val serverUrl by option(
        "--server",
        help = "Server base URL."
    ).required()
    private val projectId by option(
        "--project",
        help = "Project identifier for this measurement."
    ).required()
    private val name by option(
        "--name",
        help = "Name identifying this build."
    ).required()
    private val time by option(
        "--time",
        help = "Build time in seconds."
    ).int().required()

    override fun run() {
        println("Going to upload build time")
        val workingDir = File(".").toPath().normalize().toAbsolutePath()
        val gitHash = GitInfoCollector.getGitHash(workingDir)
        val gitDate = GitInfoCollector.getGitDate(workingDir)

        runBlocking {
            uploader.upload(
                serverUrl = serverUrl,
                endpoint = "buildTimes",
                payload = BuildTimeRequest(
                    projectId = ProjectId(projectId),
                    buildName = name,
                    gitHash = gitHash,
                    gitDate = gitDate,
                    timeSeconds = time
                )
            )
        }
        println("Build time uploaded: ${HumanReadable.duration(time.seconds)}")
    }
}