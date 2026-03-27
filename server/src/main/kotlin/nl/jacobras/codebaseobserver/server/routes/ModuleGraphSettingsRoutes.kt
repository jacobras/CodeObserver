package nl.jacobras.codebaseobserver.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import nl.jacobras.codebaseobserver.dto.ModuleGraphSettingDto
import nl.jacobras.codebaseobserver.dto.ModuleGraphSettingId
import nl.jacobras.codebaseobserver.dto.ModuleGraphSettingRequest
import nl.jacobras.codebaseobserver.dto.ModuleGraphSettingUpdateRequest
import nl.jacobras.codebaseobserver.dto.ProjectId
import nl.jacobras.codebaseobserver.server.entity.ModuleGraphSettingsTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock
import kotlin.time.Instant

internal fun Route.moduleGraphSettingsRoutes() {
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
                        id = ModuleGraphSettingId(it[ModuleGraphSettingsTable.id]),
                        createdAt = Instant.fromEpochSeconds(it[ModuleGraphSettingsTable.createdAt]),
                        projectId = ProjectId(it[ModuleGraphSettingsTable.projectId]),
                        type = it[ModuleGraphSettingsTable.type],
                        data = it[ModuleGraphSettingsTable.data]
                    )
                }
        }
        call.respond(records)
    }
    post("/moduleGraphSettings") {
        val request = call.receive<ModuleGraphSettingRequest>()
        val type = request.type.trim()
        val data = request.data.trim()
        if (type !in listOf("deprecatedModule", "forbiddenDependency")) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid type: $type"))
            return@post
        }
        if (data.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing data"))
            return@post
        }
        if (type == "forbiddenDependency" && !data.contains("->")) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid data format: $data"))
            return@post
        }
        transaction {
            ModuleGraphSettingsTable.insert {
                it[ModuleGraphSettingsTable.createdAt] = Clock.System.now().epochSeconds
                it[ModuleGraphSettingsTable.projectId] = request.projectId.value
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
        if (type == "deprecatedModule" && !data.contains("->")) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid data format: $data"))
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
}