package nl.jacobras.codebaseobserver.cli.command.measure.gradle

internal object GradlePluginParser {

    /**
     * Extracts plugin IDs from a build.gradle.kts file content.
     *
     * Supports:
     * - `alias(libs.plugins.something)` -> `libs.plugins.something`
     * - `kotlin("multiplatform")` -> `kotlin("multiplatform")`
     * - `id("com.example.plugin")` -> `com.example.plugin`
     */
    fun parse(text: String): List<String> {
        val pluginsBlockRegex = Regex("""plugins\s*\{([^}]*)}""", RegexOption.DOT_MATCHES_ALL)
        val pluginsBlock = pluginsBlockRegex.find(text)?.groupValues?.getOrNull(1) ?: return emptyList()

        val result = mutableListOf<String>()

        val aliasRegex = Regex("""alias\s*\(\s*(libs\.plugins\.[A-Za-z0-9._-]+)\s*\)""")
        aliasRegex.findAll(pluginsBlock).forEach { match ->
            result.add(match.groupValues[1])
        }

        val kotlinRegex = Regex("""kotlin\s*\(\s*"([^"]+)"\s*\)""")
        kotlinRegex.findAll(pluginsBlock).forEach { match ->
            result.add("""kotlin("${match.groupValues[1]}")""")
        }

        val idRegex = Regex("""(?<!alias\s)\bid\s*\(\s*"([^"]+)"\s*\)""")
        idRegex.findAll(pluginsBlock).forEach { match ->
            result.add(match.groupValues[1])
        }

        return result
    }
}