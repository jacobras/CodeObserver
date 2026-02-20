package nl.jacobras.codebaseobserver.cli.command.measure.gradle

internal object GradleSettingsParser {

    fun parseModules(settingsFile: String): List<String> {
        // Parse all include directives to get module names
        val includeBlockPattern = Regex("""include\s*\((.*?)\)""", RegexOption.DOT_MATCHES_ALL)
        val modulePattern = Regex(""""([^"]*)"""")

        val modules = mutableSetOf<String>()

        includeBlockPattern.findAll(settingsFile).forEach { match ->
            val includeBlock = match.groupValues[1]
            // Remove comments line-by-line to avoid apostrophes in comments interfering with quote detection
            val blockWithoutComments = includeBlock.split("\n").joinToString("\n") { line ->
                line.split("//")[0]
            }
            modulePattern.findAll(blockWithoutComments).forEach { moduleMatch ->
                val moduleName = moduleMatch.groupValues[1].removePrefix(":")
                modules.add(moduleName)
            }
        }
        return modules.sorted()
    }
}