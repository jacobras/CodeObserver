package nl.jacobras.codebaseobserver.cli.command.measure_gradle

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import nl.jacobras.codebaseobserver.cli.runCommand
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class MeasureGradleCommand : CliktCommand(name = "measure-gradle") {
    private val path by option(
        "--path",
        help = "Folder to scan. Defaults to the current working directory."
    ).default(".")
    private val serverUrl by option(
        "--server",
        help = "Server base URL. Without this, the count will not be uploaded."
    )

    override fun run() {
        val targetPath = File(path).toPath().normalize().toAbsolutePath()

        val moduleCount = countGradleModules(targetPath)
        val moduleHeight = calculateModuleHeight(targetPath)
        println("Found $moduleCount Gradle modules in $targetPath")
        println("Module graph height: $moduleHeight")

        serverUrl?.let { url ->
            val gitHash = runCommand("git", "rev-parse", "HEAD")?.trim().orEmpty()
            val gitDate = runCommand("git", "show", "-s", "--format=%cI", "HEAD")?.trim().orEmpty()

            require(gitHash.isNotEmpty()) {
                "Could not determine git hash. Make sure you are in a git repository."
            }
            require(gitDate.isNotEmpty()) {
                "Could not determine git date. Make sure you are in a git repository."
            }
            runBlocking { upload(url, gitHash, gitDate, moduleCount, moduleHeight) }
        }
    }

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

    private fun calculateModuleHeight(root: Path): Int {
        if (!Files.exists(root)) {
            println("Warning: $root does not exist")
            return 0
        }

        val settingsFile = findSettingsGradleFile(root)
        if (settingsFile == null) {
            println("Warning: settings.gradle.kts not found in $root")
            return 0
        }

        return try {
            val content = Files.readString(settingsFile)

            val modules = GradleSettingsParser.parseModules(content)
            val dependencies = mutableMapOf<String, List<String>>()

            // Parse build.gradle.kts files to find dependencies between modules
            val buildGradlePattern = Regex("""include\s*\((.*?)\)""", RegexOption.DOT_MATCHES_ALL)

            modules.forEach { module ->
                val modulePath = root.resolve(module.replace(":", File.separator))
                val buildGradle = modulePath.resolve("build.gradle.kts")
                if (Files.exists(buildGradle)) {
                    val buildContent = Files.readString(buildGradle)
                    val deps = GradleDependencyParser.parse(buildContent)
                    dependencies[module] = deps
                }
            }

            // Calculate the height of the dependency graph using topological sort
            calculateGraphHeight(modules, dependencies)
        } catch (e: Exception) {
            println("Failed to calculate module height because of ${e.message}")
            0
        }
    }

    private fun calculateGraphHeight(modules: List<String>, dependencies: Map<String, List<String>>): Int {
        if (modules.isEmpty()) return 0

        val visited = mutableSetOf<String>()
        val memo = mutableMapOf<String, Int>()

        fun dfs(module: String): Int {
            if (module in memo) return memo[module]!!
            if (module in visited) return 0 // Cycle detection

            visited.add(module)
            val height = 1 + (dependencies[module]?.maxOfOrNull { dfs(it) } ?: 0)
            visited.remove(module)

            memo[module] = height
            return height
        }

        val maxHeight = modules.maxOfOrNull { dfs(it) } ?: 0
        return maxHeight
    }

    private suspend fun upload(
        serverUrl: String,
        gitHash: String,
        gitDate: String,
        moduleCount: Int,
        moduleHeight: Int
    ) {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val payload = GradleRequest(
            gitHash = gitHash,
            gitDate = gitDate,
            moduleCount = moduleCount,
            moduleHeight = moduleHeight
        )
        val response = client.post("${serverUrl.trimEnd('/')}/gradle") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        client.close()
        val statusCode = response.status.value
        val responseBody = response.bodyAsText()
        println("Server response: $statusCode: $responseBody")
    }
}