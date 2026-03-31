package nl.jacobras.codeobserver.server.entity

import org.jetbrains.exposed.v1.core.Table

internal object MigrationProgressTable : Table("migrationProgress") {
    val migrationId = integer("migrationId")
    val gitHash = text("gitHash")
    val gitDate = long("gitDate")
    val count = integer("count")
    override val primaryKey = PrimaryKey(migrationId, gitHash)
}