package nl.jacobras.codeobserver.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import nl.jacobras.codeobserver.dto.ModuleTypeIdentifierDto
import nl.jacobras.codeobserver.dto.ModuleTypeIdentifierId
import nl.jacobras.codeobserver.dto.ModuleTypeIdentifierRequest
import nl.jacobras.codeobserver.dto.ModuleTypeIdentifierUpdateRequest
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.codeobserver.server.entity.ModuleTypeIdentifiersTable
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

internal fun Route.moduleTypeIdentifierRoutes() {
    get("/moduleTypeIdentifiers") {
        val projectId = call.request.queryParameters["projectId"]?.trim().orEmpty()
        if (projectId.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing projectId"))
            return@get
        }
        val records = transaction {
            ModuleTypeIdentifiersTable.selectAll()
                .where { ModuleTypeIdentifiersTable.projectId eq projectId }
                .orderBy(ModuleTypeIdentifiersTable.order to SortOrder.ASC)
                .map {
                    ModuleTypeIdentifierDto(
                        id = ModuleTypeIdentifierId(it[ModuleTypeIdentifiersTable.id]),
                        projectId = ProjectId(it[ModuleTypeIdentifiersTable.projectId]),
                        typeName = it[ModuleTypeIdentifiersTable.typeName],
                        plugin = it[ModuleTypeIdentifiersTable.plugin],
                        order = it[ModuleTypeIdentifiersTable.order],
                        color = it[ModuleTypeIdentifiersTable.color]
                    )
                }
        }
        call.respond(records)
    }
    post("/moduleTypeIdentifiers") {
        val request = call.receive<ModuleTypeIdentifierRequest>()
        val name = request.typeName.trim()
        val plugin = request.plugin.trim()
        val color = request.color.trim()
        if (name.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing name"))
            return@post
        }
        if (plugin.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing plugin"))
            return@post
        }
        if (color.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing color"))
            return@post
        }
        transaction {
            ModuleTypeIdentifiersTable.insert {
                it[ModuleTypeIdentifiersTable.projectId] = request.projectId.value
                it[ModuleTypeIdentifiersTable.typeName] = name
                it[ModuleTypeIdentifiersTable.plugin] = plugin
                it[ModuleTypeIdentifiersTable.order] = request.order
                it[ModuleTypeIdentifiersTable.color] = color
            }
        }
        call.respond(HttpStatusCode.Created)
    }
    patch("/moduleTypeIdentifiers/{id}") {
        val id = call.parameters["id"]?.trim()?.toIntOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing or invalid id"))
            return@patch
        }
        val request = call.receive<ModuleTypeIdentifierUpdateRequest>()
        val name = request.typeName.trim()
        val plugin = request.plugin.trim()
        val color = request.color.trim()
        if (name.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing name"))
            return@patch
        }
        if (plugin.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing plugin"))
            return@patch
        }
        if (color.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing color"))
            return@patch
        }
        val updated = transaction {
            ModuleTypeIdentifiersTable.update({ ModuleTypeIdentifiersTable.id eq id }) {
                it[ModuleTypeIdentifiersTable.typeName] = name
                it[ModuleTypeIdentifiersTable.plugin] = plugin
                it[ModuleTypeIdentifiersTable.order] = request.order
                it[ModuleTypeIdentifiersTable.color] = color
            }
        }
        if (updated == 0) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Module identifier not found"))
        } else {
            call.respond(HttpStatusCode.OK, mapOf("status" to "updated"))
        }
    }
    delete("/moduleTypeIdentifiers/{id}") {
        val id = call.parameters["id"]?.trim()?.toIntOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing or invalid id"))
            return@delete
        }
        val deleted = transaction {
            ModuleTypeIdentifiersTable.deleteWhere { ModuleTypeIdentifiersTable.id eq id }
        }
        if (deleted == 0) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Module identifier not found"))
        } else {
            call.respond(HttpStatusCode.OK, mapOf("status" to "deleted"))
        }
    }
}