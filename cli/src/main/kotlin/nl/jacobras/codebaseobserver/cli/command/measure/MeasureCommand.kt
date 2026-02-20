package nl.jacobras.codebaseobserver.cli.command.measure

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import nl.jacobras.codebaseobserver.cli.command.measure.code.MeasureCodeCommand
import nl.jacobras.codebaseobserver.cli.command.measure.gradle.MeasureGradleCommand

class MeasureCommand : CliktCommand(name = "measure") {
    private val path by option(
        "--path",
        help = "Folder to scan. Defaults to the current working directory."
    ).default(".")
    private val serverUrl by option(
        "--server",
        help = "Server base URL. Without this, the counts will not be uploaded."
    )
    private val projectId by option(
        "--project",
        help = "Project identifier for this measurement."
    ).required()

    override fun run() {
        println("Running measure-code and measure-gradle...")

        // Build arguments for subcommands
        val pathArg = "--path"
        val serverArg = "--server"
        val projectId = "--project"

        // Run measure-code
        val codeArgs = mutableListOf(pathArg, path, projectId, this@MeasureCommand.projectId)
        serverUrl?.let { codeArgs.addAll(listOf(serverArg, it)) }
        MeasureCodeCommand().main(codeArgs.toTypedArray())

        // Run measure-gradle
        val gradleArgs = mutableListOf(pathArg, path, projectId, this@MeasureCommand.projectId)
        serverUrl?.let { gradleArgs.addAll(listOf(serverArg, it)) }
        MeasureGradleCommand().main(gradleArgs.toTypedArray())
    }
}