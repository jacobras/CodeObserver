package nl.jacobras.codeobserver.server.entity

import org.jetbrains.exposed.v1.core.Table

internal object BuildTimesTable : Table("buildTimes") {
    val projectId = text("projectId")
    val buildName = text("buildName")
    val gitHash = text("gitHash")
    val gitDate = long("gitDate")
    val timeSeconds = integer("timeSeconds")
    override val primaryKey = PrimaryKey(projectId, buildName, gitHash)
}