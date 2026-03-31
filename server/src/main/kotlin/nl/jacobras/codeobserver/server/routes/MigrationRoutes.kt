package nl.jacobras.codeobserver.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import nl.jacobras.codeobserver.dto.GitHash
import nl.jacobras.codeobserver.dto.MigrationDto
import nl.jacobras.codeobserver.dto.MigrationId
import nl.jacobras.codeobserver.dto.MigrationProgressDto
import nl.jacobras.codeobserver.dto.MigrationProgressRequest
import nl.jacobras.codeobserver.dto.MigrationRequest
import nl.jacobras.codeobserver.dto.MigrationUpdateRequest
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.codeobserver.server.entity.MigrationProgressTable
import nl.jacobras.codeobserver.server.entity.MigrationsTable
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.upsert
import kotlin.time.Clock
import kotlin.time.Instant

internal fun Route.migrationRoutes() {
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
                        id = MigrationId(it[MigrationsTable.id]),
                        createdAt = Instant.fromEpochSeconds(it[MigrationsTable.createdAt]),
                        name = it[MigrationsTable.name],
                        description = it[MigrationsTable.description],
                        projectId = ProjectId(it[MigrationsTable.projectId]),
                        type = it[MigrationsTable.type],
                        rule = it[MigrationsTable.rule]
                    )
                }
        }
        call.respond(records)
    }
    post("/migrations") {
        val request = call.receive<MigrationRequest>()
        val name = request.name.trim()
        val description = request.description.trim()
        val type = request.type.trim()
        val rule = request.rule.trim()
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
                it[MigrationsTable.projectId] = request.projectId.value
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
                        migrationId = MigrationId(it[MigrationProgressTable.migrationId]),
                        gitHash = GitHash(it[MigrationProgressTable.gitHash]),
                        gitDate = Instant.fromEpochSeconds(it[MigrationProgressTable.gitDate]),
                        count = it[MigrationProgressTable.count]
                    )
                }
        }
        call.respond(records)
    }
    post("/migrationProgress") {
        val request = call.receive<MigrationProgressRequest>()
        val gitHash = request.gitHash
        transaction {
            MigrationProgressTable.upsert {
                it[MigrationProgressTable.migrationId] = request.migrationId.value
                it[MigrationProgressTable.gitHash] = gitHash.value
                it[MigrationProgressTable.gitDate] = request.gitDate.epochSeconds
                it[MigrationProgressTable.count] = request.count
            }
        }
        call.respond(HttpStatusCode.Created)
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