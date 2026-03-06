package nl.jacobras.codebaseobserver.cli.command.measure.code

internal object ImportCounter {

    /**
     * Counts occurrences of each rule (import path) in the given text.
     * Handles aliased imports such as `import com.example.Foo as Bar`.
     */
    fun count(text: String, rules: List<String>): Map<String, Int> {
        val counts = rules.associateWith { 0 }.toMutableMap()
        text.lineSequence().forEach { line ->
            val importPath = extractImportPath(line.trim()) ?: return@forEach
            for (rule in rules) {
                if (matches(rule, importPath)) counts[rule] = counts[rule]!! + 1
            }
        }
        return counts
    }

    private fun matches(rule: String, importPath: String): Boolean {
        return if (rule.endsWith(".*")) {
            importPath.startsWith(rule.removeSuffix("*"))
        } else {
            importPath == rule
        }
    }

    /**
     * Extracts the import path from a trimmed import statement line,
     * or returns null if the line is not an import statement.
     * Example: `import com.example.Foo as Bar` -> `com.example.Foo`
     */
    fun extractImportPath(trimmedLine: String): String? {
        if (!trimmedLine.startsWith("import ")) return null
        return trimmedLine.removePrefix("import ").split(Regex("\\s+")).firstOrNull()
    }
}