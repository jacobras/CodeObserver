package nl.jacobras.codeobserver.server.auth

import nl.jacobras.codeobserver.server.entity.ServerSettingsTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

internal object ApiKeyService {
    private const val SETTING_KEY = "apiKey"
    private const val KEY_BYTES = 32

    /**
     * Generates and stores an API key if none exists yet.
     * Must be called within a transaction.
     */
    fun ensureKeyExists() {
        val exists = ServerSettingsTable
            .selectAll()
            .where { ServerSettingsTable.key eq SETTING_KEY }
            .any()
        if (!exists) {
            val bytes = ByteArray(KEY_BYTES).also { SecureRandom().nextBytes(it) }
            ServerSettingsTable.insert {
                it[key] = SETTING_KEY
                it[value] = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
            }
        }
    }

    fun current(): String {
        return transaction {
            ServerSettingsTable
                .selectAll()
                .where { ServerSettingsTable.key eq SETTING_KEY }
                .single()[ServerSettingsTable.value]
        }
    }

    fun isValid(key: String): Boolean {
        return MessageDigest.isEqual(key.toByteArray(), current().toByteArray())
    }
}