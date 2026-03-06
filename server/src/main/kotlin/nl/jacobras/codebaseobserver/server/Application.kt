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
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import nl.jacobras.codebaseobserver.dto.ArtifactSizeDto
import nl.jacobras.codebaseobserver.dto.ArtifactSizeRequest
import nl.jacobras.codebaseobserver.dto.CodeMetricsDto
import nl.jacobras.codebaseobserver.dto.CodeMetricsRequest
import nl.jacobras.codebaseobserver.dto.GradleMetricsRequest
import nl.jacobras.codebaseobserver.dto.GraphModuleDto
import nl.jacobras.codebaseobserver.dto.MigrationDto
import nl.jacobras.codebaseobserver.dto.MigrationUpdateRequest
import nl.jacobras.codebaseobserver.dto.MigrationProgressDto
import nl.jacobras.codebaseobserver.dto.MigrationProgressRequest
import nl.jacobras.codebaseobserver.dto.MigrationRequest
import nl.jacobras.codebaseobserver.dto.ModuleGraphSettingDto
import nl.jacobras.codebaseobserver.dto.ModuleGraphSettingRequest
import nl.jacobras.codebaseobserver.dto.ModuleGraphSettingUpdateRequest
import nl.jacobras.codebaseobserver.dto.ModuleSortOrder
import nl.jacobras.codebaseobserver.dto.ProjectDto
import nl.jacobras.codebaseobserver.dto.ProjectRequest
import nl.jacobras.codebaseobserver.server.entity.ArtifactSizesTable
import nl.jacobras.codebaseobserver.server.entity.MetricsTable
import nl.jacobras.codebaseobserver.server.entity.MigrationProgressTable
import nl.jacobras.codebaseobserver.server.entity.MigrationsTable
import nl.jacobras.codebaseobserver.server.entity.ModuleGraphSettingsTable
import nl.jacobras.codebaseobserver.server.entity.ModuleGraphTable
import nl.jacobras.codebaseobserver.server.entity.ProjectsTable
import nl.jacobras.codebaseobserver.server.graph.GraphConfig
import nl.jacobras.codebaseobserver.server.graph.GraphUtil
import nl.jacobras.codebaseobserver.server.graph.GraphVisualizer
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.update
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
    transaction {
        SchemaUtils.create(
            ArtifactSizesTable,
            MetricsTable,
            MigrationProgressTable,
            MigrationsTable,
            ModuleGraphSettingsTable,
            ModuleGraphTable,
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
        get("/projects") {
            val projects = transaction {
                ProjectsTable
                    .selectAll()
                    .orderBy(ProjectsTable.projectId to SortOrder.ASC)
                    .map {
                        ProjectDto(
                            projectId = it[ProjectsTable.projectId],
                            name = it[ProjectsTable.name]
                        )
                    }
            }
            call.respond(projects)
        }
        post("/projects") {
            val request = call.receive<ProjectRequest>()
            val projectId = request.projectId.trim()
            val name = request.name.trim()
            if (projectId.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing projectId"))
                return@post
            }
            if (name.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing name"))
                return@post
            }

            transaction {
                ProjectsTable.upsert {
                    it[ProjectsTable.projectId] = projectId
                    it[ProjectsTable.name] = name
                }
            }
            call.respond(HttpStatusCode.Created)
        }
        delete("/projects/{projectId}") {
            val projectId = call.parameters["projectId"]?.trim().orEmpty()
            if (projectId.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing projectId"))
                return@delete
            }

            val deleted = transaction {
                MetricsTable.deleteWhere { MetricsTable.projectId eq projectId }
                ArtifactSizesTable.deleteWhere { ArtifactSizesTable.projectId eq projectId }
                ModuleGraphTable.deleteWhere { ModuleGraphTable.projectId eq projectId }
                ModuleGraphSettingsTable.deleteWhere { ModuleGraphSettingsTable.projectId eq projectId }
                val migrationIds = MigrationsTable
                    .selectAll()
                    .where { MigrationsTable.projectId eq projectId }
                    .map { it[MigrationsTable.id] }
                migrationIds.forEach { mid ->
                    MigrationProgressTable.deleteWhere { MigrationProgressTable.migrationId eq mid }
                }
                MigrationsTable.deleteWhere { MigrationsTable.projectId eq projectId }
                ProjectsTable.deleteWhere { ProjectsTable.projectId eq projectId }
            }
            if (deleted == 0) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Project not found"))
            } else {
                call.respond(HttpStatusCode.OK, mapOf("status" to "deleted"))
            }
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
            val graphConfig = transaction {
                ModuleGraphSettingsTable.selectAll()
                    .where { ModuleGraphSettingsTable.projectId eq projectId }
                    .mapNotNull {
                        when (it[ModuleGraphSettingsTable.type]) {
                            "deprecatedModule" -> GraphConfig.DeprecatedModule(it[ModuleGraphSettingsTable.data])
                            "forbiddenDependency" -> {
                                val parts = it[ModuleGraphSettingsTable.data].split(" -> ")
                                if (parts.size == 2) GraphConfig.ForbiddenDependency(parts[0], parts[1]) else null
                            }
                            else -> null
                        }
                    }
            }
            val graph = GraphVisualizer.build(
                modules = graphMap,
                startModule = startModule,
                groupingThreshold = groupingThreshold,
                layerDepth = layerDepth,
                config = graphConfig
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
        get("/migrations") {
            val projectId = call.request.queryParameters["projectId"]?.trim().orEmpty()
            if (projectId.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing projectId"))
                return@get
            }
            val records = transaction {
                MigrationsTable.selectAll()
                    .where { MigrationsTable.projectId eq projectId }
                    .map {
                        MigrationDto(
                            id = it[MigrationsTable.id],
                            createdAt = Instant.fromEpochSeconds(it[MigrationsTable.createdAt]),
                            name = it[MigrationsTable.name],
                            description = it[MigrationsTable.description],
                            projectId = it[MigrationsTable.projectId],
                            type = it[MigrationsTable.type],
                            rule = it[MigrationsTable.rule]
                        )
                    }
            }
            call.respond(records)
        }
        post("/migrations") {
            val request = call.receive<MigrationRequest>()
            val projectId = request.projectId.trim()
            val name = request.name.trim()
            val description = request.description.trim()
            val type = request.type.trim()
            val rule = request.rule.trim()
            if (projectId.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing projectId"))
                return@post
            }
            if (name.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing name"))
                return@post
            }
            if (type !in listOf("moduleUsage", "importUsage")) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid type: $type"))
                return@post
            }
            if (rule.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing rule"))
                return@post
            }
            transaction {
                MigrationsTable.insert {
                    it[MigrationsTable.createdAt] = Clock.System.now().epochSeconds
                    it[MigrationsTable.name] = name
                    it[MigrationsTable.description] = description
                    it[MigrationsTable.projectId] = projectId
                    it[MigrationsTable.type] = type
                    it[MigrationsTable.rule] = rule
                }
            }
            call.respond(HttpStatusCode.Created)
        }
        patch("/migrations/{id}") {
            val id = call.parameters["id"]?.trim()?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing or invalid id"))
                return@patch
            }
            val request = call.receive<MigrationUpdateRequest>()
            val name = request.name.trim()
            val description = request.description.trim()
            if (name.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing name"))
                return@patch
            }
            val updated = transaction {
                MigrationsTable.update({ MigrationsTable.id eq id }) {
                    it[MigrationsTable.name] = name
                    it[MigrationsTable.description] = description
                }
            }
            if (updated == 0) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Migration not found"))
            } else {
                call.respond(HttpStatusCode.OK, mapOf("status" to "updated"))
            }
        }
        delete("/migrations/{id}") {
            val id = call.parameters["id"]?.trim()?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing or invalid id"))
                return@delete
            }
            val deleted = transaction {
                MigrationProgressTable.deleteWhere { MigrationProgressTable.migrationId eq id }
                MigrationsTable.deleteWhere { MigrationsTable.id eq id }
            }
            if (deleted == 0) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Migration not found"))
            } else {
                call.respond(HttpStatusCode.OK, mapOf("status" to "deleted"))
            }
        }
        get("/migrationProgress") {
            val migrationId = call.request.queryParameters["migrationId"]?.trim()?.toIntOrNull()
            if (migrationId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing or invalid migrationId"))
                return@get
            }
            val records = transaction {
                MigrationProgressTable.selectAll()
                    .where { MigrationProgressTable.migrationId eq migrationId }
                    .orderBy(MigrationProgressTable.gitDate to SortOrder.ASC)
                    .map {
                        MigrationProgressDto(
                            migrationId = it[MigrationProgressTable.migrationId],
                            gitHash = it[MigrationProgressTable.gitHash],
                            gitDate = Instant.fromEpochSeconds(it[MigrationProgressTable.gitDate]),
                            count = it[MigrationProgressTable.count]
                        )
                    }
            }
            call.respond(records)
        }
        post("/migrationProgress") {
            val request = call.receive<MigrationProgressRequest>()
            val gitHash = request.gitHash.trim()
            if (gitHash.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing gitHash"))
                return@post
            }
            transaction {
                MigrationProgressTable.upsert {
                    it[MigrationProgressTable.migrationId] = request.migrationId
                    it[MigrationProgressTable.gitHash] = gitHash
                    it[MigrationProgressTable.gitDate] = request.gitDate.epochSeconds
                    it[MigrationProgressTable.count] = request.count
                }
            }
            call.respond(HttpStatusCode.Created)
        }
        get("/moduleGraphSettings") {
            val projectId = call.request.queryParameters["projectId"]?.trim().orEmpty()
            if (projectId.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing projectId"))
                return@get
            }
            val records = transaction {
                ModuleGraphSettingsTable.selectAll()
                    .where { ModuleGraphSettingsTable.projectId eq projectId }
                    .map {
                        ModuleGraphSettingDto(
                            id = it[ModuleGraphSettingsTable.id],
                            createdAt = Instant.fromEpochSeconds(it[ModuleGraphSettingsTable.createdAt]),
                            projectId = it[ModuleGraphSettingsTable.projectId],
                            type = it[ModuleGraphSettingsTable.type],
                            data = it[ModuleGraphSettingsTable.data]
                        )
                    }
            }
            call.respond(records)
        }
        post("/moduleGraphSettings") {
            val request = call.receive<ModuleGraphSettingRequest>()
            val projectId = request.projectId.trim()
            val type = request.type.trim()
            val data = request.data.trim()
            if (projectId.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing projectId"))
                return@post
            }
            if (type !in listOf("deprecatedModule", "forbiddenDependency")) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid type: $type"))
                return@post
            }
            if (data.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing data"))
                return@post
            }
            transaction {
                ModuleGraphSettingsTable.insert {
                    it[ModuleGraphSettingsTable.createdAt] = Clock.System.now().epochSeconds
                    it[ModuleGraphSettingsTable.projectId] = projectId
                    it[ModuleGraphSettingsTable.type] = type
                    it[ModuleGraphSettingsTable.data] = data
                }
            }
            call.respond(HttpStatusCode.Created)
        }
        patch("/moduleGraphSettings/{id}") {
            val id = call.parameters["id"]?.trim()?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing or invalid id"))
                return@patch
            }
            val request = call.receive<ModuleGraphSettingUpdateRequest>()
            val type = request.type.trim()
            val data = request.data.trim()
            if (type !in listOf("deprecatedModule", "forbiddenDependency")) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid type: $type"))
                return@patch
            }
            if (data.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing data"))
                return@patch
            }
            val updated = transaction {
                ModuleGraphSettingsTable.update({ ModuleGraphSettingsTable.id eq id }) {
                    it[ModuleGraphSettingsTable.type] = type
                    it[ModuleGraphSettingsTable.data] = data
                }
            }
            if (updated == 0) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Setting not found"))
            } else {
                call.respond(HttpStatusCode.OK, mapOf("status" to "updated"))
            }
        }
        delete("/moduleGraphSettings/{id}") {
            val id = call.parameters["id"]?.trim()?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing or invalid id"))
                return@delete
            }
            val deleted = transaction {
                ModuleGraphSettingsTable.deleteWhere { ModuleGraphSettingsTable.id eq id }
            }
            if (deleted == 0) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Setting not found"))
            } else {
                call.respond(HttpStatusCode.OK, mapOf("status" to "deleted"))
            }
        }
        delete("/migrationProgress/{migrationId}/{gitHash}") {
            val migrationId = call.parameters["migrationId"]?.trim()?.toIntOrNull()
            val gitHash = call.parameters["gitHash"]?.trim().orEmpty()
            if (migrationId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing or invalid migrationId"))
                return@delete
            }
            if (gitHash.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing gitHash"))
                return@delete
            }
            val deleted = transaction {
                MigrationProgressTable.deleteWhere {
                    (MigrationProgressTable.migrationId eq migrationId) and (MigrationProgressTable.gitHash eq gitHash)
                }
            }
            if (deleted == 0) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Record not found"))
            } else {
                call.respond(HttpStatusCode.OK, mapOf("status" to "deleted"))
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