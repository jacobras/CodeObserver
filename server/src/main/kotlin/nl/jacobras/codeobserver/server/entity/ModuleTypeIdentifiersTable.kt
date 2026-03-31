package nl.jacobras.codeobserver.server.entity

import org.jetbrains.exposed.v1.core.Table

internal object ModuleTypeIdentifiersTable : Table("moduleTypeIdentifiers") {
    val id = integer("id").autoIncrement()
    val projectId = text("projectId")
    val typeName = text("name")
    val plugin = text("plugin")
    val order = integer("sortOrder")
    val color = text("color")
    override val primaryKey = PrimaryKey(id)
}