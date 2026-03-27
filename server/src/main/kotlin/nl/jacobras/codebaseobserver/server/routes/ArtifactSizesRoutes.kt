package nl.jacobras.codebaseobserver.server.routes

import io.github.z4kn4fein.semver.toVersionOrNull
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import nl.jacobras.codebaseobserver.dto.ArtifactSizeDto
import nl.jacobras.codebaseobserver.dto.ArtifactSizeRequest
import nl.jacobras.codebaseobserver.dto.ProjectId
import nl.jacobras.codebaseobserver.server.entity.ArtifactSizesTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert
import kotlin.time.Clock
import kotlin.time.Instant

internal fun Route.artifactSizeRoutes() {
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
                        projectId = ProjectId(it[ArtifactSizesTable.projectId]),
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
                it[projectId] = request.projectId.value
                it[createdAt] = Clock.System.now().epochSeconds
                it[name] = request.name
                it[semVer] = request.semVer
                it[size] = request.size
            }
        }
        call.respond(HttpStatusCode.Created)
    }
}