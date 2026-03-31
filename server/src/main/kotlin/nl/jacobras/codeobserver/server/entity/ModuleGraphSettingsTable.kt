package nl.jacobras.codeobserver.server.entity

import org.jetbrains.exposed.v1.core.Table

internal object ModuleGraphSettingsTable : Table("moduleGraphSettings") {
    val id = integer("id").autoIncrement()
    val createdAt = long("createdAt")
    val projectId = text("projectId")
    val type = text("type")
    val data = text("data")
    override val primaryKey = PrimaryKey(id)
}