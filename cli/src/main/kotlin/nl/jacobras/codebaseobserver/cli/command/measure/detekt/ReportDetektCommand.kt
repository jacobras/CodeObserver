package nl.jacobras.codebaseobserver.cli.command.measure.detekt

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import kotlinx.coroutines.runBlocking
import nl.jacobras.codebaseobserver.cli.util.GitInfoCollector
import nl.jacobras.codebaseobserver.cli.util.ServerUploader
import nl.jacobras.codebaseobserver.dto.DetektReportRequest
import java.io.File

class ReportDetektCommand internal constructor(
    private val uploader: ServerUploader
) : CliktCommand(name = "report-detekt") {
    private val htmlFile by option(
        "--htmlFile",
        help = "Path to the Detekt HTML report file."
    ).required()
    private val serverUrl by option(
        "--server",
        help = "Server base URL."
    ).required()
    private val projectId by option(
        "--project",
        help = "Project identifier for this measurement."
    ).required()

    override fun run() {
        val file = File(htmlFile)
        val htmlContent = file.readText()

        val findingsMatch = Regex("<li>([\\d,]+) number of total code smells</li>").find(htmlContent)
        val findings = findingsMatch?.groupValues?.get(1)?.replace(",", "")?.toInt() ?: 0

        val smellsMatch = Regex("<li>([\\d,]+) code smells per 1,000 lloc</li>").find(htmlContent)
        val smellsPer1000 = smellsMatch?.groupValues?.get(1)?.replace(",", "")?.toInt() ?: 0

        val workingDir = File(".").toPath().normalize().toAbsolutePath()
        val gitHash = GitInfoCollector.getGitHash(workingDir)
        val gitDate = GitInfoCollector.getGitDate(workingDir)

        runBlocking {
            uploader.upload(
                serverUrl = serverUrl,
                endpoint = "detektReports",
                payload = DetektReportRequest(
                    projectId = projectId,
                    gitHash = gitHash,
                    gitDate = gitDate,
                    findings = findings,
                    smellsPer1000 = smellsPer1000,
                    htmlReport = htmlContent
                )
            )
        }
        println("Detekt report uploaded: $findings findings, $smellsPer1000 smells/1000 lloc")
    }
}