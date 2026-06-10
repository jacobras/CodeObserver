package nl.jacobras.codeobserver.server.auth

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import nl.jacobras.codeobserver.dto.UserRole

internal data class UserPrincipal(
    val username: String,
    val role: UserRole
)

internal object ApiKeyPrincipal

/**
 * Returns the authenticated admin or responds 403.
 */
internal suspend fun ApplicationCall.requireAdmin(): UserPrincipal? {
    val principal = principal<UserPrincipal>()
    if (principal?.role != UserRole.ADMIN) {
        respond(HttpStatusCode.Forbidden, mapOf("error" to "Admin role required"))
        return null
    }
    return principal
}