package nl.jacobras.codebaseobserver.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import nl.jacobras.codebaseobserver.dto.DetektMetricDto
import nl.jacobras.codebaseobserver.dto.DetektReportRequest
import nl.jacobras.codebaseobserver.dto.GitHash
import nl.jacobras.codebaseobserver.dto.ProjectId
import nl.jacobras.codebaseobserver.dto.ReportId
import nl.jacobras.codebaseobserver.server.entity.DetektReportsTable
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert
import kotlin.time.Instant

internal fun Route.detektReportRoutes() {
    get("/detektMetrics") {
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
                    DetektMetricDto(
                        id = ReportId(it[DetektReportsTable.id]),
                        projectId = ProjectId(it[DetektReportsTable.projectId]),
                        gitHash = GitHash(it[DetektReportsTable.gitHash]),
                        gitDate = Instant.fromEpochSeconds(it[DetektReportsTable.gitDate]),
                        findings = it[DetektReportsTable.findings],
                        smellsPer1000 = it[DetektReportsTable.smellsPer1000]
                    )
                }
        }
        call.respond(records)
    }
    get("/detektReports/{reportId}") {
        val reportId = call.parameters["reportId"]!!.trim().toIntOrNull()
        if (reportId == null) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing reportId"))
            return@get
        }
        val record = transaction {
            DetektReportsTable.selectAll()
                .where { DetektReportsTable.id eq reportId }
                .firstNotNullOfOrNull {
                    it[DetektReportsTable.htmlReport]
                }
        }
        if (record == null) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Report not found"))
            return@get
        }
        call.respond(record)
    }
    post("/detektReports") {
        val request = call.receive<DetektReportRequest>()
        transaction {
            DetektReportsTable.upsert {
                it[projectId] = request.projectId.value
                it[gitHash] = request.gitHash.value
                it[gitDate] = request.gitDate.epochSeconds
                it[findings] = request.findings
                it[smellsPer1000] = request.smellsPer1000
                it[htmlReport] = request.htmlReport
            }
        }
        call.respond(HttpStatusCode.Created)
    }
    delete("/detektReports/{reportId}") {
        val reportId = call.parameters["reportId"]!!.trim()
        if (reportId.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing reportId"))
            return@delete
        }
        val deleted = transaction {
            DetektReportsTable.deleteWhere {
                (DetektReportsTable.id eq reportId.toInt())
            }
        }
        if (deleted == 0) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Report not found"))
        } else {
            call.respond(HttpStatusCode.OK, mapOf("status" to "deleted"))
        }
    }
}