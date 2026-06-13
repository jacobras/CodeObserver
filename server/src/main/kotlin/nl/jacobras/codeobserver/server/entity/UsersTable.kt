package nl.jacobras.codeobserver.server.entity

import org.jetbrains.exposed.v1.core.Table

internal object UsersTable : Table("users") {
    val username = text("username")
    val passwordHash = text("passwordHash")
    val role = text("role")
    override val primaryKey = PrimaryKey(username)
}