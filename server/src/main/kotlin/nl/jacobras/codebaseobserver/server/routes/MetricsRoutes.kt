package nl.jacobras.codebaseobserver.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.json.Json
import nl.jacobras.codebaseobserver.dto.CodeMetricsDto
import nl.jacobras.codebaseobserver.dto.CodeMetricsRequest
import nl.jacobras.codebaseobserver.dto.GradleMetricsRequest
import nl.jacobras.codebaseobserver.server.entity.MetricsTable
import nl.jacobras.codebaseobserver.server.entity.ModuleGraphTable
import nl.jacobras.codebaseobserver.server.verifyGitInfo
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
        val error = verifyGitInfo(projectId = request.projectId, gitHash = request.gitHash)
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
        val error = verifyGitInfo(projectId = request.projectId, gitHash = request.gitHash)
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
                it[moduleDetails] = request.moduleDetails
            }
        }
        call.respond(HttpStatusCode.Created)
    }
    delete("/metrics/{gitHash}") {
        val gitHash = call.parameters["gitHash"]?.trim().orEmpty()
        val projectId = call.request.queryParameters["projectId"]?.trim().orEmpty()
        val error = verifyGitInfo(projectId = projectId, gitHash = gitHash)
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
}