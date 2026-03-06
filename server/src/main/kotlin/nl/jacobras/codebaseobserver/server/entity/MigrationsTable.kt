package nl.jacobras.codebaseobserver.server.entity

import org.jetbrains.exposed.v1.core.Table

internal object MigrationsTable : Table("migrations") {
    val id = integer("id").autoIncrement()
    val createdAt = long("createdAt")
    val name = text("name")
    val description = text("description")
    val projectId = text("projectId")
    val type = text("type")
    val rule = text("rule")
    override val primaryKey = PrimaryKey(id)
}