package nl.jacobras.codeobserver.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.sessions.clear
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import nl.jacobras.codeobserver.dto.LoginRequest
import nl.jacobras.codeobserver.dto.UserDto
import nl.jacobras.codeobserver.dto.UserRole
import nl.jacobras.codeobserver.server.auth.PasswordHasher
import nl.jacobras.codeobserver.server.auth.UserSession
import nl.jacobras.codeobserver.server.entity.UsersTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

internal fun Route.authRoutes() {
    post("/login") {
        val request = call.receive<LoginRequest>()
        val user = transaction {
            UsersTable
                .selectAll()
                .where { UsersTable.username eq request.username }
                .singleOrNull()
        }
        // Verify against a dummy hash for unknown users to keep timing uniform.
        val hash = user?.get(UsersTable.passwordHash) ?: PasswordHasher.dummyHash
        val verified = PasswordHasher.verify(request.password, hash)
        if (user == null || !verified) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid username or password"))
            return@post
        }

        call.sessions.set(UserSession(user[UsersTable.username]))
        call.respond(
            UserDto(
                username = user[UsersTable.username],
                role = UserRole.valueOf(user[UsersTable.role])
            )
        )
    }

    // Deliberately outside the authenticated-shielded block in UserRoutes: logging out without a
    // valid session must succeed, or expired sessions can never log out.
    post("/logout") {
        call.sessions.clear<UserSession>()
        call.respond(HttpStatusCode.OK, mapOf("status" to "loggedOut"))
    }
}