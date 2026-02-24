package nl.jacobras.codebaseobserver.cli.command.measure.gradle

internal object GradleSettingsParser {

    fun parseModules(text: String): List<String> {
        val moduleRegex = Regex(""""(:[^"]+)"""")

        return moduleRegex.findAll(text)
            .map { it.groupValues[1].removePrefix(":") }
            .toList()
    }
}