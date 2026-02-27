package nl.jacobras.codebaseobserver.cli.command.measure.gradle

internal object GradleDependencyParser {

    /**
     * Extracts the `implementation()` dependencies from `text`.
     *
     * @param accessorMapping A mapping from Gradle project accessors to module IDs. This is needed because
     * kebab-case (some-lib) and snake-case (some_lib) are converted to camel case (someLib) names.
     * (see [docs](https://docs.gradle.org/current/userguide/declaring_dependencies_basics.html#sec:type-safe-project-accessors)).
     */
    fun parse(
        text: String,
        accessorMapping: Map<String, String>
    ): List<String> {
        val result = mutableListOf<String>()

        // projects.a.b
        val projectsRegex = Regex("""projects\.([A-Za-z0-9_.-]+)""")
        projectsRegex.findAll(text).forEach { match ->
            val path = match.groupValues[1]
            val pathOriginal = accessorMapping[path] ?: path
            result.add(dotPathToColon(pathOriginal))
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