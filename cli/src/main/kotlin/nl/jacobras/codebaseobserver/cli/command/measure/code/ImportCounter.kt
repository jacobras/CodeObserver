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
            if (importPath in counts) counts[importPath] = counts[importPath]!! + 1
        }
        return counts
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