package nl.jacobras.codebaseobserver.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import nl.jacobras.codebaseobserver.dto.BuildTimeDto
import nl.jacobras.codebaseobserver.dto.BuildTimeRequest
import nl.jacobras.codebaseobserver.server.entity.BuildTimesTable
import nl.jacobras.codebaseobserver.server.verifyGitInfo
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert
import kotlin.time.Instant

internal fun Route.buildTimeRoutes() {
    get("/buildTimes") {
        val projectId = call.request.queryParameters["projectId"]?.trim().orEmpty()
        if (projectId.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing projectId"))
            return@get
        }
        val records = transaction {
            BuildTimesTable.selectAll()
                .where { BuildTimesTable.projectId eq projectId }
                .orderBy(BuildTimesTable.gitDate to SortOrder.ASC)
                .map {
                    BuildTimeDto(
                        projectId = it[BuildTimesTable.projectId],
                        buildName = it[BuildTimesTable.buildName],
                        gitHash = it[BuildTimesTable.gitHash],
                        gitDate = Instant.fromEpochSeconds(it[BuildTimesTable.gitDate]),
                        timeSeconds = it[BuildTimesTable.timeSeconds]
                    )
                }
        }
        call.respond(records)
    }
    post("/buildTimes") {
        val request = call.receive<BuildTimeRequest>()
        val error = verifyGitInfo(projectId = request.projectId, gitHash = request.gitHash)
        if (error.isNotEmpty()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to error))
            return@post
        }
        if (request.buildName.isEmpty()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing buildName"))
            return@post
        }
        transaction {
            BuildTimesTable.upsert {
                it[projectId] = request.projectId
                it[buildName] = request.buildName
                it[gitHash] = request.gitHash
                it[gitDate] = request.gitDate.epochSeconds
                it[timeSeconds] = request.timeSeconds
            }
        }
        call.respond(HttpStatusCode.Created)
    }
}