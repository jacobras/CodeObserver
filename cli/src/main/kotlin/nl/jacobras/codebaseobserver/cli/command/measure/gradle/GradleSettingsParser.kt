package nl.jacobras.codebaseobserver.cli.command.measure.gradle

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
        return modules.associate { module ->
            val parts = module.split(":")

            // Convert the last part to camelCase for the key (split on _ or -)
            val keyLastPart = parts.last().split("_", "-").mapIndexed { index, s ->
                if (index == 0) s else s.replaceFirstChar { it.uppercaseChar() }
            }.joinToString("")

            val key = parts.dropLast(1).plus(keyLastPart).joinToString(".")

            // Convert underscores to hyphens for the value, keep existing hyphens
            val valueLastPart = parts.last().replace("_", "-")
            val value = parts.dropLast(1).plus(valueLastPart).joinToString(":")

            key to value
        }
    }
}