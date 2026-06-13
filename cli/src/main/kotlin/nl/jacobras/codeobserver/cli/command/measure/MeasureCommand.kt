package nl.jacobras.codeobserver.cli.command.measure

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import nl.jacobras.codeobserver.cli.command.measure.code.MeasureCodeCommand
import nl.jacobras.codeobserver.cli.command.measure.gradle.MeasureGradleCommand
import nl.jacobras.codeobserver.cli.util.ServerUploader

class MeasureCommand : CliktCommand(name = "measure") {
    private val serverUrl by option(
        "--server",
        help = "Server base URL. Without this, the counts will not be uploaded."
    )
    private val projectId by option(
        "--project",
        help = "Project identifier for this measurement."
    ).required()
    private val path by option(
        "--path",
        help = "Folder to scan. Defaults to the current working directory."
    ).default(".")
    private val apiKey by option(
        "--api-key",
        envvar = "CODEOBSERVER_API_KEY",
        help = "Server API key. Can also be set via the CODEOBSERVER_API_KEY environment variable."
    )

    override fun run() {
        println("Running measure-code and measure-gradle...")

        // Build arguments for subcommands
        val pathArg = "--path"
        val serverArg = "--server"
        val apiKeyArg = "--api-key"
        val projectId = "--project"

        val uploader = ServerUploader()

        // Run measure-code
        val codeArgs = mutableListOf(pathArg, path, projectId, this@MeasureCommand.projectId)
        serverUrl?.let { codeArgs.addAll(listOf(serverArg, it)) }
        apiKey?.let { codeArgs.addAll(listOf(apiKeyArg, it)) }
        MeasureCodeCommand(uploader).main(codeArgs.toTypedArray())

        // Run measure-gradle
        val gradleArgs = mutableListOf(pathArg, path, projectId, this@MeasureCommand.projectId)
        serverUrl?.let { gradleArgs.addAll(listOf(serverArg, it)) }
        apiKey?.let { gradleArgs.addAll(listOf(apiKeyArg, it)) }
        MeasureGradleCommand(uploader).main(gradleArgs.toTypedArray())
    }
}