package nl.jacobras.codebaseobserver.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import nl.jacobras.codebaseobserver.cli.command.measure.MeasureCommand
import nl.jacobras.codebaseobserver.cli.command.measure_code.MeasureCodeCommand
import nl.jacobras.codebaseobserver.cli.command.measure_gradle.MeasureGradleCommand

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
        subcommands(MeasureCommand(), MeasureCodeCommand(), MeasureGradleCommand())
    }

    override fun run() = Unit
}