package nl.jacobras.codeobserver.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.json.Json
import nl.jacobras.codeobserver.dto.CodeMetricsDto
import nl.jacobras.codeobserver.dto.CodeMetricsRequest
import nl.jacobras.codeobserver.dto.GitHash
import nl.jacobras.codeobserver.dto.GradleMetricsRequest
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.codeobserver.server.entity.MetricsTable
import nl.jacobras.codeobserver.server.entity.ModuleGraphTable
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert
import kotlin.time.Clock
import kotlin.time.Instant

internal fun Route.metricRoutes() {
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
                        projectId = ProjectId(it[MetricsTable.projectId]),
                        createdAt = Instant.fromEpochSeconds(it[MetricsTable.createdAt]),
                        gitHash = GitHash(it[MetricsTable.gitHash]),
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
                onUpdateExclude = listOf(
                    MetricsTable.createdAt,
                    MetricsTable.moduleCount,
                    MetricsTable.moduleTreeHeight
                )
            ) {
                it[projectId] = request.projectId.value
                it[createdAt] = Clock.System.now().epochSeconds
                it[gitHash] = request.gitHash.value
                it[gitDate] = request.gitDate.epochSeconds
                it[linesOfCode] = request.linesOfCode
            }
        }
        call.respond(HttpStatusCode.Created)
    }
    post("/metrics/gradle") {
        val request = call.receive<GradleMetricsRequest>()
        transaction {
            MetricsTable.upsert(
                onUpdateExclude = listOf(
                    MetricsTable.createdAt,
                    MetricsTable.linesOfCode
                )
            ) {
                it[projectId] = request.projectId.value
                it[createdAt] = Clock.System.now().epochSeconds
                it[gitHash] = request.gitHash.value
                it[gitDate] = request.gitDate.epochSeconds
                it[moduleCount] = request.moduleCount
                it[moduleTreeHeight] = request.longestPath.size
            }
            ModuleGraphTable.upsert(
                onUpdateExclude = listOf(ModuleGraphTable.createdAt)
            ) {
                it[projectId] = request.projectId.value
                it[createdAt] = Clock.System.now().epochSeconds
                it[gitHash] = request.gitHash.value
                it[gitDate] = request.gitDate.epochSeconds
                it[graph] = Json.encodeToString(request.graph)
                it[moduleDetails] = request.moduleDetails
                it[longestPath] = request.longestPath.joinToString(separator = ",")
            }
        }
        call.respond(HttpStatusCode.Created)
    }
    delete("/metrics/{gitHash}") {
        val gitHash = call.parameters["gitHash"]?.trim().orEmpty()
        val projectId = call.request.queryParameters["projectId"]?.trim().orEmpty()
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