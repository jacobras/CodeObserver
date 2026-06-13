package nl.jacobras.codeobserver.server.auth

import at.favre.lib.crypto.bcrypt.BCrypt

internal object PasswordHasher {

    /**
     * Hash used to keep login timing uniform for unknown usernames.
     */
    val dummyHash: String by lazy { hash("dummy-password") }

    fun hash(password: String): String {
        return BCrypt.withDefaults().hashToString(COST, password.toCharArray())
    }

    fun verify(password: String, hash: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), hash.toCharArray()).verified
    }
}

private const val COST = 12