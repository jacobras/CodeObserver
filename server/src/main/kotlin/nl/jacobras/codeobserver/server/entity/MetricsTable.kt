package nl.jacobras.codeobserver.server.entity

import org.jetbrains.exposed.v1.core.Table

internal object MetricsTable : Table("metrics") {
    val createdAt = long("createdAt")
    val projectId = text("projectId")
    val gitHash = text("gitHash")
    val gitDate = long("gitDate")
    val linesOfCode = integer("linesOfCode").default(0)
    val moduleCount = integer("moduleCount").default(0)
    val moduleTreeHeight = integer("moduleTreeHeight").default(0)
    override val primaryKey = PrimaryKey(projectId, gitHash)
}