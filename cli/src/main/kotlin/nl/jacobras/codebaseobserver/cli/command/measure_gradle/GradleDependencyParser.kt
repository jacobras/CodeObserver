package nl.jacobras.codebaseobserver.cli.command.measure_gradle

internal object GradleDependencyParser {

    fun parse(buildFile: String): List<String> {
        val deps = mutableListOf<String>()

        // Pattern 1: projects.a.b.c -> a:b:c
        val projectsPattern = Regex("""projects(?:\.(\w+))+""")
        projectsPattern.findAll(buildFile).forEach { match ->
            val fullMatch = match.value
            val parts = fullMatch.substring("projects".length).split(".").filter { it.isNotEmpty() }
            deps.add(parts.joinToString(":"))
        }

        // Pattern 2: ":module:path" or ":module" -> module:path or module
        val stringLiteralPattern = Regex("""":([\w:]+)"""")
        stringLiteralPattern.findAll(buildFile).forEach { match ->
            deps.add(match.groupValues[1])
        }

        return deps
    }
}