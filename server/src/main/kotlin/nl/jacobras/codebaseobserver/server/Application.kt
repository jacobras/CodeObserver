package nl.jacobras.codebaseobserver.server

import io.github.z4kn4fein.semver.toVersionOrNull
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import nl.jacobras.codebaseobserver.dto.ArtifactSizeDto
import nl.jacobras.codebaseobserver.dto.ArtifactSizeRequest
import nl.jacobras.codebaseobserver.dto.CodeMetricsDto
import nl.jacobras.codebaseobserver.dto.CodeMetricsRequest
import nl.jacobras.codebaseobserver.dto.GradleMetricsRequest
import nl.jacobras.codebaseobserver.dto.GraphModuleDto
import nl.jacobras.codebaseobserver.dto.ModuleSortOrder
import nl.jacobras.codebaseobserver.server.entity.ArtifactSizesTable
import nl.jacobras.codebaseobserver.server.entity.MetricsTable
import nl.jacobras.codebaseobserver.server.entity.ModuleGraphTable
import nl.jacobras.codebaseobserver.server.graph.GraphUtil
import nl.jacobras.codebaseobserver.server.graph.GraphVisualizer
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert
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
    transaction { SchemaUtils.create(MetricsTable, ArtifactSizesTable, ModuleGraphTable) }

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
                        CodeMetricsDto(
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
            val error = verifyBasicInfo(projectId = request.projectId, gitHash = request.gitHash)
            if (error.isNotEmpty()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to error))
                return@post
            }
            transaction {
                MetricsTable.upsert(
                    onUpdateExclude = listOf(
                        MetricsTable.createdAt,
                        MetricsTable.moduleCount,
                        MetricsTable.moduleTreeHeight
                    )
                ) {
                    it[projectId] = request.projectId
                    it[createdAt] = Clock.System.now().epochSeconds
                    it[gitHash] = request.gitHash
                    it[gitDate] = request.gitDate.epochSeconds
                    it[linesOfCode] = request.linesOfCode
                }
            }
            call.respond(HttpStatusCode.Created)
        }
        post("/metrics/gradle") {
            val request = call.receive<GradleMetricsRequest>()
            val error = verifyBasicInfo(projectId = request.projectId, gitHash = request.gitHash)
            if (error.isNotEmpty()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to error))
                return@post
            }
            transaction {
                MetricsTable.upsert(
                    onUpdateExclude = listOf(
                        MetricsTable.createdAt,
                        MetricsTable.linesOfCode
                    )
                ) {
                    it[projectId] = request.projectId
                    it[createdAt] = Clock.System.now().epochSeconds
                    it[gitHash] = request.gitHash
                    it[gitDate] = request.gitDate.epochSeconds
                    it[moduleCount] = request.moduleCount
                    it[moduleTreeHeight] = request.moduleTreeHeight
                }
                ModuleGraphTable.upsert(
                    onUpdateExclude = listOf(ModuleGraphTable.createdAt)
                ) {
                    it[projectId] = request.projectId
                    it[createdAt] = Clock.System.now().epochSeconds
                    it[gitHash] = request.gitHash
                    it[gitDate] = request.gitDate.epochSeconds
                    it[graph] = Json.encodeToString(request.graph)
                }
            }
            call.respond(HttpStatusCode.Created)
        }
        delete("/metrics/{gitHash}") {
            val gitHash = call.parameters["gitHash"]?.trim().orEmpty()
            val projectId = call.request.queryParameters["projectId"]?.trim().orEmpty()
            val error = verifyBasicInfo(projectId = projectId, gitHash = gitHash)
            if (error.isNotEmpty()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to error))
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
        get("/artifactSizes") {
            val projectId = call.request.queryParameters["projectId"]?.trim().orEmpty()
            if (projectId.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing projectId"))
                return@get
            }
            val records = transaction {
                ArtifactSizesTable.selectAll()
                    .where { ArtifactSizesTable.projectId eq projectId }
                    .map {
                        ArtifactSizeDto(
                            projectId = it[ArtifactSizesTable.projectId],
                            createdAt = Instant.fromEpochSeconds(it[ArtifactSizesTable.createdAt]),
                            name = it[ArtifactSizesTable.name],
                            semVer = it[ArtifactSizesTable.semVer],
                            size = it[ArtifactSizesTable.size]
                        )
                    }
            }
            call.respond(records)
        }
        post("/artifactSizes") {
            val request = call.receive<ArtifactSizeRequest>()
            if (request.projectId.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing projectId"))
                return@post
            }
            if (request.name.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing name"))
                return@post
            }
            if (request.semVer.toVersionOrNull() == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid semVer: ${request.semVer}"))
                return@post
            }
            if (request.size == 0L) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing size"))
                return@post
            }
            transaction {
                ArtifactSizesTable.upsert(
                    onUpdateExclude = listOf(ArtifactSizesTable.createdAt)
                ) {
                    it[projectId] = request.projectId
                    it[createdAt] = Clock.System.now().epochSeconds
                    it[name] = request.name
                    it[semVer] = request.semVer
                    it[size] = request.size
                }
            }
            call.respond(HttpStatusCode.Created)
        }
        get("/moduleGraph") {
            val projectId = call.request.queryParameters["projectId"]?.trim().orEmpty()
            if (projectId.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing projectId"))
                return@get
            }
            val startModule = call.request.queryParameters["startModule"]?.trim().orEmpty()
            val groupingThreshold = call.request.queryParameters["groupingThreshold"]?.trim()?.toIntOrNull()
            if (groupingThreshold == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing groupingThreshold"))
                return@get
            }
            val layerDepth = call.request.queryParameters["layerDepth"]?.trim()?.toIntOrNull() ?: 30

            val graphRecord = transaction {
                ModuleGraphTable
                    .selectAll()
                    .where { ModuleGraphTable.projectId eq projectId }
                    .orderBy(ModuleGraphTable.gitDate to SortOrder.DESC)
                    .limit(1)
                    .singleOrNull()
            }
            if (graphRecord == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Record not found"))
                return@get
            }

            val graphMap = Json.decodeFromString<Map<String, List<String>>>(graphRecord[ModuleGraphTable.graph])
            val graph = GraphVisualizer.build(
                modules = graphMap,
                startModule = startModule,
                groupingThreshold = groupingThreshold,
                layerDepth = layerDepth
            )
            call.respondText(graph)
        }
        get("/modules") {
            val projectId = call.request.queryParameters["projectId"]?.trim().orEmpty()
            if (projectId.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing projectId"))
                return@get
            }
            val sortOrderId = call.request.queryParameters["sort"]?.trim().orEmpty()
            val sortOrder = ModuleSortOrder.fromId(sortOrderId) ?: run {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Unsupported sort order: $sortOrderId")
                )
                return@get
            }

            val graphRecord = transaction {
                ModuleGraphTable
                    .selectAll()
                    .where { ModuleGraphTable.projectId eq projectId }
                    .orderBy(ModuleGraphTable.gitDate to SortOrder.DESC)
                    .limit(1)
                    .singleOrNull()
            }
            if (graphRecord == null) {
                call.respond(emptyList<GraphModuleDto>())
                return@get
            }

            val graphMap = Json.decodeFromString<Map<String, List<String>>>(graphRecord[ModuleGraphTable.graph])

            when (sortOrder) {
                ModuleSortOrder.BetweennessCentrality -> {
                    val betweennessCentrality = GraphUtil.calculateBetweennessCentralityScore(graphMap)
                    call.respond(betweennessCentrality.map { GraphModuleDto(it.key, it.value.toInt()) })
                }
                ModuleSortOrder.Alphabetical -> {
                    val modules = (graphMap.keys + graphMap.values.flatten()).distinct().sorted()
                    call.respond(modules.map { GraphModuleDto(it, 0) })
                }
            }
        }
    }
}

/**
 * Verifies that the request contains all required fields.
 * @return error message.
 */
private fun verifyBasicInfo(projectId: String, gitHash: String): String {
    if (projectId.isEmpty()) {
        return "Missing projectId"
    }
    if (gitHash.isEmpty()) {
        return "Missing gitHash"
    }
    return ""
}