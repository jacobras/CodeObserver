package nl.jacobras.codeobserver.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import nl.jacobras.codeobserver.dto.ApiKeyDto
import nl.jacobras.codeobserver.dto.ChangePasswordRequest
import nl.jacobras.codeobserver.dto.CreateUserRequest
import nl.jacobras.codeobserver.dto.UpdateUserRequest
import nl.jacobras.codeobserver.dto.UserDto
import nl.jacobras.codeobserver.dto.UserRole
import nl.jacobras.codeobserver.server.auth.ApiKeyService
import nl.jacobras.codeobserver.server.auth.PasswordHasher
import nl.jacobras.codeobserver.server.auth.UserPrincipal
import nl.jacobras.codeobserver.server.auth.requireAdmin
import nl.jacobras.codeobserver.server.entity.SessionsTable
import nl.jacobras.codeobserver.server.entity.UsersTable
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

private enum class MutationResult { Ok, NotFound, LastAdmin }

internal fun Route.userRoutes() {
    get("/me") {
        val principal = call.principal<UserPrincipal>() ?: run {
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Not logged in"))
            return@get
        }
        call.respond(UserDto(username = principal.username, role = principal.role))
    }

    put("/me/password") {
        val principal = call.principal<UserPrincipal>() ?: run {
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Not logged in"))
            return@put
        }
        val request = call.receive<ChangePasswordRequest>()
        if (request.newPassword.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Password may not be blank"))
            return@put
        }

        val currentHash = transaction {
            UsersTable
                .selectAll()
                .where { UsersTable.username eq principal.username }
                .single()[UsersTable.passwordHash]
        }
        if (!PasswordHasher.verify(request.currentPassword, currentHash)) {
            call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Current password is incorrect"))
            return@put
        }

        transaction {
            UsersTable.update({ UsersTable.username eq principal.username }) {
                it[passwordHash] = PasswordHasher.hash(request.newPassword)
            }
        }
        call.respond(HttpStatusCode.OK, mapOf("status" to "updated"))
    }

    get("/users") {
        call.requireAdmin() ?: return@get
        val users = transaction {
            UsersTable
                .selectAll()
                .orderBy(UsersTable.username to SortOrder.ASC)
                .map {
                    UserDto(
                        username = it[UsersTable.username],
                        role = UserRole.valueOf(it[UsersTable.role])
                    )
                }
        }
        call.respond(users)
    }

    post("/users") {
        call.requireAdmin() ?: return@post
        val request = call.receive<CreateUserRequest>()
        val username = request.username.trim()
        if (username.isEmpty() || request.password.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Username and password may not be blank"))
            return@post
        }
        if (username.contains('/')) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Username may not contain '/'"))
            return@post
        }

        val created = transaction {
            val exists = UsersTable
                .selectAll()
                .where { UsersTable.username eq username }
                .any()
            if (exists) {
                false
            } else {
                UsersTable.insert {
                    it[UsersTable.username] = username
                    it[passwordHash] = PasswordHasher.hash(request.password)
                    it[role] = request.role.name
                }
                true
            }
        }
        if (created) {
            call.respond(HttpStatusCode.Created, UserDto(username = username, role = request.role))
        } else {
            call.respond(HttpStatusCode.Conflict, mapOf("error" to "Username already exists"))
        }
    }

    put("/users/{username}") {
        call.requireAdmin() ?: return@put
        val username = call.parameters["username"]?.trim().orEmpty()
        val request = call.receive<UpdateUserRequest>()
        val newRole = request.role
        val newPassword = request.password
        if (newPassword != null && newPassword.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Password may not be blank"))
            return@put
        }

        val result = transaction {
            val target = UsersTable
                .selectAll()
                .where { UsersTable.username eq username }
                .singleOrNull()
                ?: return@transaction MutationResult.NotFound
            val demotesAdmin = target[UsersTable.role] == UserRole.Admin.name &&
                newRole != null && newRole != UserRole.Admin
            if (demotesAdmin && adminCount() <= 1) {
                return@transaction MutationResult.LastAdmin
            }

            UsersTable.update({ UsersTable.username eq username }) {
                if (newRole != null) {
                    it[role] = newRole.name
                }
                if (newPassword != null) {
                    it[passwordHash] = PasswordHasher.hash(newPassword)
                }
            }
            MutationResult.Ok
        }
        call.respondMutationResult(result)
    }

    delete("/users/{username}") {
        call.requireAdmin() ?: return@delete
        val username = call.parameters["username"]?.trim().orEmpty()

        val result = transaction {
            val target = UsersTable
                .selectAll()
                .where { UsersTable.username eq username }
                .singleOrNull()
                ?: return@transaction MutationResult.NotFound
            if (target[UsersTable.role] == UserRole.Admin.name && adminCount() <= 1) {
                return@transaction MutationResult.LastAdmin
            }

            UsersTable.deleteWhere { UsersTable.username eq username }
            SessionsTable.deleteWhere { SessionsTable.username eq username }
            MutationResult.Ok
        }
        call.respondMutationResult(result)
    }

    get("/apiKey") {
        call.requireAdmin() ?: return@get
        call.respond(ApiKeyDto(key = ApiKeyService.current()))
    }
}

private fun adminCount(): Long {
    return UsersTable
        .selectAll()
        .where { UsersTable.role eq UserRole.Admin.name }
        .count()
}

private suspend fun ApplicationCall.respondMutationResult(result: MutationResult) {
    when (result) {
        MutationResult.Ok -> respond(HttpStatusCode.OK, mapOf("status" to "ok"))
        MutationResult.NotFound -> respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
        MutationResult.LastAdmin -> respond(
            HttpStatusCode.Conflict,
            mapOf("error" to "There must always be at least one admin")
        )
    }
}