package nl.jacobras.codebaseobserver.server.entity

import org.jetbrains.exposed.v1.core.Table

internal object DetektReportsTable : Table("detektReports") {
    val id = integer("id").autoIncrement()
    val projectId = text("projectId")
    val gitHash = text("gitHash")
    val gitDate = long("gitDate")
    val findings = integer("findings")
    val smellsPer1000 = integer("smellsPer1000")
    val htmlReport = text("htmlReport")
    override val primaryKey = PrimaryKey(id)
}