package nl.jacobras.codeobserver.server.entity

import org.jetbrains.exposed.v1.core.Table

internal object SessionsTable : Table("sessions") {
    val id = text("id")
    val username = text("username")
    val value = text("value")
    val expiresAt = long("expiresAt")
    override val primaryKey = PrimaryKey(id)
}