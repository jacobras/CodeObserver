package nl.jacobras.codeobserver.server.entity

import org.jetbrains.exposed.v1.core.Table

internal object ProjectsTable : Table("projects") {
    val projectId = text("projectId")
    val name = text("name")
    override val primaryKey = PrimaryKey(projectId)
}