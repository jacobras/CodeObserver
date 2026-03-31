package nl.jacobras.codeobserver.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import nl.jacobras.codeobserver.dto.ProjectDto
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.codeobserver.dto.ProjectRequest
import nl.jacobras.codeobserver.server.entity.ArtifactSizesTable
import nl.jacobras.codeobserver.server.entity.BuildTimesTable
import nl.jacobras.codeobserver.server.entity.DetektReportsTable
import nl.jacobras.codeobserver.server.entity.MetricsTable
import nl.jacobras.codeobserver.server.entity.MigrationProgressTable
import nl.jacobras.codeobserver.server.entity.MigrationsTable
import nl.jacobras.codeobserver.server.entity.ModuleGraphSettingsTable
import nl.jacobras.codeobserver.server.entity.ModuleGraphTable
import nl.jacobras.codeobserver.server.entity.ModuleTypeIdentifiersTable
import nl.jacobras.codeobserver.server.entity.ProjectsTable
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert

internal fun Route.projectRoutes() {
    get("/projects") {
        val projects = transaction {
            ProjectsTable
                .selectAll()
                .orderBy(ProjectsTable.projectId to SortOrder.ASC)
                .map {
                    ProjectDto(
                        id = ProjectId(it[ProjectsTable.projectId]),
                        name = it[ProjectsTable.name]
                    )
                }
        }
        call.respond(projects)
    }
    post("/projects") {
        val request = call.receive<ProjectRequest>()
        val name = request.name.trim()
        if (name.isEmpty()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing name"))
            return@post
        }

        transaction {
            ProjectsTable.upsert {
                it[ProjectsTable.projectId] = request.projectId.value
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
            BuildTimesTable.deleteWhere { BuildTimesTable.projectId eq projectId }
            DetektReportsTable.deleteWhere { DetektReportsTable.projectId eq projectId }
            ModuleGraphTable.deleteWhere { ModuleGraphTable.projectId eq projectId }
            ModuleGraphSettingsTable.deleteWhere { ModuleGraphSettingsTable.projectId eq projectId }
            ModuleTypeIdentifiersTable.deleteWhere { ModuleTypeIdentifiersTable.projectId eq projectId }
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
}