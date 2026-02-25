package nl.jacobras.codebaseobserver.server.entity

import org.jetbrains.exposed.v1.core.Table

internal object ModuleGraphTable : Table("moduleGraph") {
    val createdAt = long("createdAt")
    val projectId = text("projectId")
    val gitHash = text("gitHash")
    val gitDate = long("gitDate")
    val graph = text("graph")
    override val primaryKey = PrimaryKey(projectId, gitHash)
}