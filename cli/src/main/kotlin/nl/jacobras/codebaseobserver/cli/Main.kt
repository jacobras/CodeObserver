package nl.jacobras.codebaseobserver.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import nl.jacobras.codebaseobserver.cli.command.measure.MeasureCommand
import nl.jacobras.codebaseobserver.cli.command.measure.code.MeasureCodeCommand
import nl.jacobras.codebaseobserver.cli.command.measure.gradle.MeasureGradleCommand
import nl.jacobras.codebaseobserver.cli.util.ServerUploader

fun main(args: Array<String>) {
    val rootCommand = RootCommand()
    if (args.isEmpty()) {
        rootCommand.main(arrayOf("--help"))
    } else {
        rootCommand.main(args)
    }
}

private class RootCommand : CliktCommand(name = "codebaseobserver") {
    init {
        val uploader = ServerUploader()
        subcommands(
            MeasureCommand(),
            MeasureCodeCommand(uploader),
            MeasureGradleCommand(uploader)
        )
    }

    override fun run() = Unit
}