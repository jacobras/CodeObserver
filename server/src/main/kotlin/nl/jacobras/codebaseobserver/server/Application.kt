package nl.jacobras.codebaseobserver.server

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import nl.jacobras.codebaseobserver.server.entity.ArtifactSizesTable
import nl.jacobras.codebaseobserver.server.entity.BuildTimesTable
import nl.jacobras.codebaseobserver.server.entity.MetricsTable
import nl.jacobras.codebaseobserver.server.entity.MigrationProgressTable
import nl.jacobras.codebaseobserver.server.entity.MigrationsTable
import nl.jacobras.codebaseobserver.server.entity.ModuleGraphSettingsTable
import nl.jacobras.codebaseobserver.server.entity.ModuleGraphTable
import nl.jacobras.codebaseobserver.server.entity.ModuleTypeIdentifiersTable
import nl.jacobras.codebaseobserver.server.entity.ProjectsTable
import nl.jacobras.codebaseobserver.server.routes.artifactSizeRoutes
import nl.jacobras.codebaseobserver.server.routes.buildTimeRoutes
import nl.jacobras.codebaseobserver.server.routes.metricRoutes
import nl.jacobras.codebaseobserver.server.routes.migrationRoutes
import nl.jacobras.codebaseobserver.server.routes.moduleGraphSettingsRoutes
import nl.jacobras.codebaseobserver.server.routes.moduleRoutes
import nl.jacobras.codebaseobserver.server.routes.moduleTypeIdentifierRoutes
import nl.jacobras.codebaseobserver.server.routes.projectRoutes
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.io.File

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = false
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (cause.message ?: "Unknown error"))
            )
        }
    }

    val dbPath = System.getenv("DB_PATH") ?: "data/app.db"
    val dbFile = File(dbPath)
    dbFile.parentFile?.mkdirs()
    Database.connect("jdbc:sqlite:${dbFile.absolutePath}", driver = "org.sqlite.JDBC")
    transaction {
        SchemaUtils.create(
            ArtifactSizesTable,
            BuildTimesTable,
            MetricsTable,
            MigrationProgressTable,
            MigrationsTable,
            ModuleGraphSettingsTable,
            ModuleGraphTable,
            ModuleTypeIdentifiersTable,
            ProjectsTable,
        )
    }

    routing {
        staticFiles("/", File("app/web")) {
            default("index.html")
        }
        staticFiles("/dev", File("../web/build/dist/wasmJs/developmentExecutable")) {
            default("index.html")
        }
        projectRoutes()
        metricRoutes()
        artifactSizeRoutes()
        buildTimeRoutes()
        moduleRoutes()
        migrationRoutes()
        moduleGraphSettingsRoutes()
        moduleTypeIdentifierRoutes()
    }
}

/**
 * Verifies that the request contains all required fields.
 * @return error message.
 */
internal fun verifyGitInfo(projectId: String, gitHash: String): String {
    if (projectId.isEmpty()) {
        return "Missing projectId"
    }
    if (gitHash.isEmpty()) {
        return "Missing gitHash"
    }
    return ""
}