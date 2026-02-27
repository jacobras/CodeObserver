package nl.jacobras.codebaseobserver.cli.command.measure.gradle

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import kotlinx.coroutines.runBlocking
import nl.jacobras.codebaseobserver.cli.util.GitInfoCollector
import nl.jacobras.codebaseobserver.cli.util.ServerUploader
import nl.jacobras.codebaseobserver.dto.GradleMetricsRequest
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class MeasureGradleCommand internal constructor(
    private val uploader: ServerUploader
) : CliktCommand(name = "measure-gradle") {
    private val path by option(
        "--path",
        help = "Folder to scan. Defaults to the current working directory."
    ).default(".")
    private val serverUrl by option(
        "--server",
        help = "Server base URL. Without this, the count will not be uploaded."
    )
    private val projectId by option(
        "--project",
        help = "Project identifier for this measurement."
    ).required()

    override fun run() {
        println("Going to measure gradle modules")
        val targetPath = File(path).toPath().normalize().toAbsolutePath()

        val moduleCount = countGradleModules(targetPath)
        val graphInfo = calculateModuleTreeHeightWithPath(targetPath)
        val moduleTreeHeight = graphInfo.height
        val longestPath = graphInfo.longestPath
        println("Found $moduleCount Gradle modules in $targetPath")
        println("Module tree height: $moduleTreeHeight")
        if (longestPath.isNotEmpty()) {
            println("Longest path: ${longestPath.joinToString(" --> ")}")
        }

        serverUrl?.let { url ->
            val payload = GradleMetricsRequest(
                projectId = projectId,
                gitHash = GitInfoCollector.getGitHash(targetPath),
                gitDate = GitInfoCollector.getGitDate(targetPath),
                moduleCount = moduleCount,
                moduleTreeHeight = moduleTreeHeight,
                graph = graphInfo.graph
            )
            runBlocking {
                uploader.upload(
                    serverUrl = url,
                    endpoint = "metrics/gradle",
                    payload = payload
                )
            }
        }
    }

    private data class ModuleGraphInfo(
        val height: Int,
        val longestPath: List<String>,
        val graph: Map<String, List<String>>
    )

    private fun countGradleModules(root: Path): Int {
        if (!Files.exists(root)) {
            println("Warning: $root does not exist")
            return 0
        }

        // Find settings.gradle.kts file
        val settingsFile = findSettingsGradleFile(root)
        if (settingsFile == null) {
            println("Warning: settings.gradle.kts not found in $root")
            return 0
        }

        // Count include directives in settings.gradle.kts
        return countIncludeDirectives(settingsFile)
    }

    private fun findSettingsGradleFile(root: Path): Path? {
        val settingsPath = root.resolve("settings.gradle.kts")
        return if (Files.exists(settingsPath)) {
            settingsPath
        } else {
            null
        }
    }

    private fun countIncludeDirectives(settingsFile: Path): Int {
        return try {
            val content = Files.readString(settingsFile)
            // Extract all include(...) calls, handling multi-line statements
            // Match include( ... ) where ... can span multiple lines
            val includeBlockPattern = Regex("""include\s*\((.*?)\)""", RegexOption.DOT_MATCHES_ALL)
            var moduleCount = 0

            includeBlockPattern.findAll(content).forEach { match ->
                val includeBlock = match.groupValues[1]
                // Count quoted strings (module names) within this include block
                // Matches patterns like: "moduleA", ":moduleB", 'module', ':module'
                val modulePattern = Regex("""["']([^"']+)["']""")
                moduleCount += modulePattern.findAll(includeBlock).count()
            }

            moduleCount
        } catch (e: Exception) {
            println("Failed to count modules because of ${e.message}")
            0
        }
    }

    private fun calculateModuleTreeHeightWithPath(root: Path): ModuleGraphInfo {
        if (!Files.exists(root)) {
            println("Warning: $root does not exist")
            return ModuleGraphInfo(0, emptyList(), emptyMap())
        }

        val settingsFile = findSettingsGradleFile(root)
        if (settingsFile == null) {
            println("Warning: settings.gradle.kts not found in $root")
            return ModuleGraphInfo(0, emptyList(), emptyMap())
        }

        return try {
            val content = Files.readString(settingsFile)

            val modules = GradleSettingsParser.parseModules(content)
            val dependencies = mutableMapOf<String, List<String>>()

            modules.forEach { module ->
                val modulePath = root.resolve(module.replace(":", File.separator))
                val buildGradle = modulePath.resolve("build.gradle.kts")
                if (Files.exists(buildGradle)) {
                    val buildContent = Files.readString(buildGradle)
                    val deps = GradleDependencyParser.parse(buildContent)
                    dependencies[module] = deps
                } else {
                    dependencies.putIfAbsent(module, emptyList())
                }
            }

            val (height, path) = calculateGraphHeight(modules, dependencies)
            ModuleGraphInfo(height, path, dependencies.toMap())
        } catch (e: Exception) {
            println("Failed to calculate module height because of ${e.message}")
            ModuleGraphInfo(0, emptyList(), emptyMap())
        }
    }

    private fun calculateGraphHeight(
        modules: List<String>,
        dependencies: Map<String, List<String>>
    ): Pair<Int, List<String>> {
        if (modules.isEmpty()) return Pair(0, emptyList())

        val visited = mutableSetOf<String>()
        val memo = mutableMapOf<String, Int>()
        val pathMemo = mutableMapOf<String, List<String>>()

        fun dfs(module: String): Pair<Int, List<String>> {
            if (module in memo) return Pair(memo[module]!!, pathMemo[module]!!)
            if (module in visited) return Pair(0, emptyList()) // Cycle detection

            visited.add(module)
            val deps = dependencies[module] ?: emptyList()

            if (deps.isEmpty()) {
                visited.remove(module)
                memo[module] = 1
                pathMemo[module] = listOf(module)
                return Pair(1, listOf(module))
            }

            val (maxHeight, longestPath) = deps
                .map { dfs(it) }
                .maxByOrNull { it.first } ?: Pair(0, emptyList())

            val height = 1 + maxHeight
            val path = listOf(module) + longestPath

            visited.remove(module)
            memo[module] = height
            pathMemo[module] = path
            return Pair(height, path)
        }

        val results = modules.map { dfs(it) }
        val maxResult = results.maxByOrNull { it.first } ?: Pair(0, emptyList())
        return maxResult
    }
}