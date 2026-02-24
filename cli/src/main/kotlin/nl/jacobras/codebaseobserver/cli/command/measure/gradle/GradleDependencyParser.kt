package nl.jacobras.codebaseobserver.cli.command.measure.gradle

internal object GradleDependencyParser {

    fun parse(text: String): List<String> {
        val result = mutableListOf<String>()

        // projects.a.b
        val projectsRegex = Regex("""projects\.([A-Za-z0-9_.-]+)""")
        projectsRegex.findAll(text).forEach { match ->
            val path = match.groupValues[1]
            result.add(dotPathToColon(path))
        }

        // project(":module:sub")
        val projectStringRegex = Regex("""project\("(:[^"]+)"\)""")
        projectStringRegex.findAll(text).forEach { match ->
            val path = match.groupValues[1].removePrefix(":")
            result.add(path)
        }
        return result
    }

    // Convert project accessor to colon-separated module ID.
    private fun dotPathToColon(path: String): String {
        val segments = path.split(".")
        return if (segments.size <= 1) {
            segments.first()
        } else {
            segments.joinToString(":")
        }
    }
}