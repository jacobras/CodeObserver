package nl.jacobras.codeobserver.server.entity

import org.jetbrains.exposed.v1.core.Table

internal object ModuleGraphTable : Table("moduleGraph") {
    val createdAt = long("createdAt")
    val projectId = text("projectId")
    val gitHash = text("gitHash")
    val gitDate = long("gitDate")
    val graph = text("graph")
    val moduleDetails = text("moduleDetails").default("")
    val longestPath = text("longestPath").default("")
    override val primaryKey = PrimaryKey(projectId)
}