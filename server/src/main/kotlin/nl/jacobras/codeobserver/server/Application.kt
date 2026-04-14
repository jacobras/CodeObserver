package nl.jacobras.codeobserver.server

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
import nl.jacobras.codeobserver.server.entity.ArtifactSizesTable
import nl.jacobras.codeobserver.server.entity.BuildTimesTable
import nl.jacobras.codeobserver.server.entity.DetektReportsTable
import nl.jacobras.codeobserver.server.entity.MetricsTable
import nl.jacobras.codeobserver.server.entity.MigrationProgressTable
import nl.jacobras.codeobserver.server.entity.MigrationsTable
import nl.jacobras.codeobserver.server.entity.ModuleGraphSettingsTable
import nl.jacobras.codeobserver.server.entity.ModuleGraphTable
import nl.jacobras.codeobserver.server.entity.ModuleTypeIdentifiersTable
import nl.jacobras.codeobserver.server.entity.ProjectsTable
import nl.jacobras.codeobserver.server.routes.artifactSizeRoutes
import nl.jacobras.codeobserver.server.routes.buildTimeRoutes
import nl.jacobras.codeobserver.server.routes.detektReportRoutes
import nl.jacobras.codeobserver.server.routes.metricRoutes
import nl.jacobras.codeobserver.server.routes.migrationRoutes
import nl.jacobras.codeobserver.server.routes.moduleGraphSettingsRoutes
import nl.jacobras.codeobserver.server.routes.moduleRoutes
import nl.jacobras.codeobserver.server.routes.moduleTypeIdentifierRoutes
import nl.jacobras.codeobserver.server.routes.projectRoutes
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.io.File

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module(
    webRoot: File = File("app/web"),
    defaultDbPath: String = "data/app.db"
) {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = false
                isLenient = true
                ignoreUnknownKeys = true
            }
        )
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (cause.message ?: "Unknown error"))
            )
        }
    }

    val dbPath = System.getenv("DB_PATH") ?: defaultDbPath
    val dbFile = File(dbPath)
    dbFile.parentFile?.mkdirs()
    Database.connect("jdbc:sqlite:${dbFile.absolutePath}", driver = "org.sqlite.JDBC")
    transaction {
        SchemaUtils.create(
            ArtifactSizesTable,
            BuildTimesTable,
            DetektReportsTable,
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
        staticFiles("/", webRoot) {
            default("index.html")
        }
        staticFiles("/dev", File("../web/build/dist/wasmJs/developmentExecutable")) {
            default("index.html")
        }
        projectRoutes()
        metricRoutes()
        artifactSizeRoutes()
        buildTimeRoutes()
        detektReportRoutes()
        moduleRoutes()
        migrationRoutes()
        moduleGraphSettingsRoutes()
        moduleTypeIdentifierRoutes()
    }
}