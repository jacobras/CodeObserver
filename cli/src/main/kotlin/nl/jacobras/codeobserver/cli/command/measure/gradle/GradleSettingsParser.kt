package nl.jacobras.codeobserver.cli.command.measure.gradle

internal object GradleSettingsParser {

    /**
     * Extracts the modules from the Gradle settings file.
     */
    fun parseModules(text: String): List<String> {
        val moduleRegex = Regex(""""(:[^"]+)"""")

        return moduleRegex.findAll(text)
            .map { it.groupValues[1].removePrefix(":") }
            .toList()
    }

    /**
     * Creates a mapping of typesafe accessor paths to their original paths.
     * For example, "a:my-module" turns into "a.myModule".
     */
    fun parseAccessorMapping(modules: List<String>): Map<String, String> {
        fun String.toCamelCase(): String =
            split("_", "-")
                .mapIndexed { index, part ->
                    if (index == 0) part
                    else part.replaceFirstChar { it.uppercaseChar() }
                }
                .joinToString("")

        return modules.associate { module ->
            val parts = module.split(":")

            // CamelCase ALL segments for the key
            val key = parts.joinToString(".") { it.toCamelCase() }

            // Only normalize underscores → hyphens for the VALUE last part
            val valueLastPart = parts.last().replace("_", "-")
            val value = parts.dropLast(1)
                .plus(valueLastPart)
                .joinToString(":")

            key to value
        }
    }
}