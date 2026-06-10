package nl.jacobras.codeobserver.server.entity

import org.jetbrains.exposed.v1.core.Table

internal object ServerSettingsTable : Table("serverSettings") {
    val key = text("key")
    val value = text("value")
    override val primaryKey = PrimaryKey(key)
}