package nl.jacobras.codeobserver.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import nl.jacobras.codeobserver.cli.command.measure.MeasureCommand
import nl.jacobras.codeobserver.cli.command.measure.artifact.MeasureArtifactCommand
import nl.jacobras.codeobserver.cli.command.measure.buildtime.ReportBuildTimeCommand
import nl.jacobras.codeobserver.cli.command.measure.code.MeasureCodeCommand
import nl.jacobras.codeobserver.cli.command.measure.detekt.ReportDetektCommand
import nl.jacobras.codeobserver.cli.command.measure.gradle.MeasureGradleCommand
import nl.jacobras.codeobserver.cli.util.ServerUploader

fun main(args: Array<String>) {
    val rootCommand = RootCommand()
    if (args.isEmpty()) {
        rootCommand.main(arrayOf("--help"))
    } else {
        rootCommand.main(args)
    }
}

private class RootCommand : CliktCommand(name = "codeobserver") {
    init {
        val uploader = ServerUploader()
        subcommands(
            MeasureCommand(),
            MeasureCodeCommand(uploader),
            MeasureGradleCommand(uploader),
            MeasureArtifactCommand(uploader),
            ReportBuildTimeCommand(uploader),
            ReportDetektCommand(uploader)
        )
    }

    override fun run() = Unit
}