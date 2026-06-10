package nl.jacobras.codeobserver.server.auth

import io.ktor.server.sessions.SessionStorage
import kotlinx.serialization.json.Json
import nl.jacobras.codeobserver.server.entity.SessionsTable
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert

internal class SqliteSessionStorage : SessionStorage {

    override suspend fun write(id: String, value: String) {
        // See SessionSerializer for the other (serialization) side of this.
        val username = runCatching { Json.decodeFromString<UserSession>(value).username }.getOrDefault("")
        transaction {
            SessionsTable.upsert {
                it[SessionsTable.id] = id
                it[SessionsTable.username] = username
                it[SessionsTable.value] = value
                it[expiresAt] = System.currentTimeMillis() + SESSION_TTL_MS
            }
        }
    }

    override suspend fun read(id: String): String {
        return transaction {
            SessionsTable
                .selectAll()
                .where { (SessionsTable.id eq id) and (SessionsTable.expiresAt greater System.currentTimeMillis()) }
                .singleOrNull()
                ?.get(SessionsTable.value)
        } ?: throw NoSuchElementException("Session $id not found")
    }

    override suspend fun invalidate(id: String) {
        transaction {
            SessionsTable.deleteWhere { SessionsTable.id eq id }
        }
    }
}

internal fun purgeExpiredSessions() {
    transaction {
        SessionsTable.deleteWhere { SessionsTable.expiresAt lessEq System.currentTimeMillis() }
    }
}

internal const val SESSION_TTL_MS = 30L * 24 * 60 * 60 * 1000