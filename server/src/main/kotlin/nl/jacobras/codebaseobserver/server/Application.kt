package nl.jacobras.codebaseobserver.server

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import nl.jacobras.codebaseobserver.dto.CodeMetricsRequest
import nl.jacobras.codebaseobserver.dto.GradleMetricsRequest
import nl.jacobras.codebaseobserver.dto.MetricsDto
import nl.jacobras.codebaseobserver.server.entity.MetricsTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert
import java.io.File
import kotlin.time.Clock
import kotlin.time.Instant

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
    install(CORS) { // TODO remove?
        anyHost()
        allowHeader(HttpHeaders.ContentType)
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
    transaction { SchemaUtils.create(MetricsTable) }

    routing {
        staticFiles("/", File("app/web")) {
            default("index.html")
        }
        staticFiles("/dev", File("../web/build/dist/wasmJs/developmentExecutable")) {
            default("index.html")
        }
        get("/projects") {
            val projects = transaction {
                MetricsTable
                    .select(MetricsTable.projectId)
                    .withDistinct()
                    .map { it[MetricsTable.projectId] }
                    .sorted()
            }
            call.respond(projects)
        }
        get("/metrics") {
            val projectId = call.request.queryParameters["projectId"]?.trim().orEmpty()
            if (projectId.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing projectId"))
                return@get
            }
            val records = transaction {
                MetricsTable.selectAll()
                    .where { MetricsTable.projectId eq projectId }
                    .orderBy(MetricsTable.gitDate to SortOrder.ASC)
                    .map {
                        MetricsDto(
                            projectId = it[MetricsTable.projectId],
                            createdAt = Instant.fromEpochSeconds(it[MetricsTable.createdAt]),
                            gitHash = it[MetricsTable.gitHash],
                            gitDate = Instant.fromEpochSeconds(it[MetricsTable.gitDate]),
                            linesOfCode = it[MetricsTable.linesOfCode],
                            moduleCount = it[MetricsTable.moduleCount],
                            moduleTreeHeight = it[MetricsTable.moduleTreeHeight]
                        )
                    }
            }
            call.respond(records)
        }
        post("/metrics/code") {
            val request = call.receive<CodeMetricsRequest>()
            transaction {
                MetricsTable.upsert(
                    onUpdateExclude = listOf(MetricsTable.moduleCount, MetricsTable.moduleTreeHeight)
                ) {
                    it[projectId] = request.projectId
                    it[createdAt] = Clock.System.now().epochSeconds
                    it[gitHash] = request.gitHash
                    it[gitDate] = request.gitDate.epochSeconds
                    it[linesOfCode] = request.linesOfCode
                }
            }
            call.respond(HttpStatusCode.Created, mapOf("status" to "stored"))
        }
        post("/metrics/gradle") {
            val request = call.receive<GradleMetricsRequest>()
            transaction {
                MetricsTable.upsert(
                    onUpdateExclude = listOf(MetricsTable.linesOfCode)
                ) {
                    it[projectId] = request.projectId
                    it[createdAt] = Clock.System.now().epochSeconds
                    it[gitHash] = request.gitHash
                    it[gitDate] = request.gitDate.epochSeconds
                    it[moduleCount] = request.moduleCount
                    it[moduleTreeHeight] = request.moduleTreeHeight
                }
            }
            call.respond(HttpStatusCode.Created, mapOf("status" to "stored"))
        }
        delete("/metrics/{gitHash}") {
            val gitHash = call.parameters["gitHash"]
            if (gitHash.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing gitHash"))
                return@delete
            }
            val projectId = call.request.queryParameters["projectId"]?.trim().orEmpty()
            if (projectId.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing projectId"))
                return@delete
            }
            val deletedRows = transaction {
                MetricsTable.deleteWhere { (MetricsTable.projectId eq projectId) and (MetricsTable.gitHash eq gitHash) }
            }
            if (deletedRows == 0) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Record not found"))
            } else {
                call.respond(HttpStatusCode.OK, mapOf("status" to "deleted"))
            }
        }
    }
}