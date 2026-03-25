package nl.jacobras.codebaseobserver.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import nl.jacobras.codebaseobserver.dto.DetektReportDto
import nl.jacobras.codebaseobserver.dto.DetektReportRequest
import nl.jacobras.codebaseobserver.server.entity.DetektReportsTable
import nl.jacobras.codebaseobserver.server.verifyGitInfo
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert
import kotlin.time.Instant

internal fun Route.detektReportRoutes() {
    get("/detektReports") {
        val projectId = call.request.queryParameters["projectId"]?.trim().orEmpty()
        if (projectId.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing projectId"))
            return@get
        }
        val records = transaction {
            DetektReportsTable.selectAll()
                .where { DetektReportsTable.projectId eq projectId }
                .orderBy(DetektReportsTable.gitDate to SortOrder.ASC)
                .map {
                    DetektReportDto(
                        projectId = it[DetektReportsTable.projectId],
                        gitHash = it[DetektReportsTable.gitHash],
                        gitDate = Instant.fromEpochSeconds(it[DetektReportsTable.gitDate]),
                        findings = it[DetektReportsTable.findings],
                        smellsPer1000 = it[DetektReportsTable.smellsPer1000],
                        htmlReport = it[DetektReportsTable.htmlReport]
                    )
                }
        }
        call.respond(records)
    }
    post("/detektReports") {
        val request = call.receive<DetektReportRequest>()
        val error = verifyGitInfo(projectId = request.projectId, gitHash = request.gitHash)
        if (error.isNotEmpty()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to error))
            return@post
        }
        transaction {
            DetektReportsTable.upsert {
                it[projectId] = request.projectId
                it[gitHash] = request.gitHash
                it[gitDate] = request.gitDate.epochSeconds
                it[findings] = request.findings
                it[smellsPer1000] = request.smellsPer1000
                it[htmlReport] = request.htmlReport
            }
        }
        call.respond(HttpStatusCode.Created)
    }
}