package nl.jacobras.codebaseobserver.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands

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
        subcommands(MeasureCodeCommand())
    }

    override fun run() = Unit
}