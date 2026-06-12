package nl.jacobras.codeobserver.server.auth

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import nl.jacobras.codeobserver.dto.ApiKeyDto
import nl.jacobras.codeobserver.dto.ChangePasswordRequest
import nl.jacobras.codeobserver.dto.CreateUserRequest
import nl.jacobras.codeobserver.dto.LoginRequest
import nl.jacobras.codeobserver.dto.UpdateUserRequest
import nl.jacobras.codeobserver.dto.UserDto
import nl.jacobras.codeobserver.dto.UserRole
import nl.jacobras.codeobserver.server.module
import java.io.File
import kotlin.test.Test

class AuthRoutesTest {

    private fun authTestApplication(block: suspend ApplicationTestBuilder.(HttpClient) -> Unit) {
        val dbFile = File.createTempFile("codeobserver-test", ".db").also { it.deleteOnExit() }
        testApplication {
            application {
                module(defaultDbPath = dbFile.absolutePath)
            }
            val client = createClient {
                install(ContentNegotiation) {
                    json()
                }
                install(HttpCookies)
            }
            block(client)
        }
    }

    private suspend fun HttpClient.login(username: String, password: String): HttpResponse {
        return post("/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username = username, password = password))
        }
    }

    @Test
    fun `fresh install adds default admin account`() = authTestApplication { client ->
        val response = client.login("admin", "admin")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<UserDto>()).isEqualTo(UserDto(username = "admin", role = UserRole.ADMIN))
    }

    @Test
    fun `login with wrong password is rejected`() = authTestApplication { client ->
        val response = client.login("admin", "wrong")

        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `data routes require authentication`() = authTestApplication { client ->
        assertThat(client.get("/projects").status).isEqualTo(HttpStatusCode.Unauthorized)
        assertThat(client.get("/me").status).isEqualTo(HttpStatusCode.Unauthorized)

        client.login("admin", "admin")

        assertThat(client.get("/projects").status).isEqualTo(HttpStatusCode.OK)
        assertThat(client.get("/me").body<UserDto>().username).isEqualTo("admin")
    }

    @Test
    fun `data routes accept a valid api key`() = authTestApplication { client ->
        client.login("admin", "admin")
        val apiKey = client.get("/apiKey").body<ApiKeyDto>().key

        val anonymousClient = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        val withKey = anonymousClient.get("/projects") {
            header("X-Api-Key", apiKey)
        }
        val withWrongKey = anonymousClient.get("/projects") {
            header("X-Api-Key", "invalid")
        }

        assertThat(withKey.status).isEqualTo(HttpStatusCode.OK)
        assertThat(withWrongKey.status).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `logout invalidates the session`() = authTestApplication { client ->
        client.login("admin", "admin")
        assertThat(client.get("/me").status).isEqualTo(HttpStatusCode.OK)

        client.post("/logout")

        assertThat(client.get("/me").status).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `logout without a session succeeds`() = authTestApplication { client ->
        assertThat(client.post("/logout").status).isEqualTo(HttpStatusCode.OK)
    }

    @Test
    fun `admin can manage users`() = authTestApplication { client ->
        client.login("admin", "admin")

        val created = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest(username = "dev", password = "secret", role = UserRole.DEVELOPER))
        }
        assertThat(created.status).isEqualTo(HttpStatusCode.Created)

        val duplicate = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest(username = "dev", password = "other", role = UserRole.DEVELOPER))
        }
        assertThat(duplicate.status).isEqualTo(HttpStatusCode.Conflict)

        val blank = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest(username = "", password = "secret", role = UserRole.DEVELOPER))
        }
        assertThat(blank.status).isEqualTo(HttpStatusCode.BadRequest)

        val users = client.get("/users").body<List<UserDto>>()
        assertThat(users).isEqualTo(
            listOf(
                UserDto(username = "admin", role = UserRole.ADMIN),
                UserDto(username = "dev", role = UserRole.DEVELOPER)
            )
        )

        val deleted = client.delete("/users/dev")
        assertThat(deleted.status).isEqualTo(HttpStatusCode.OK)
    }

    @Test
    fun `developer cannot manage users or projects`() = authTestApplication { client ->
        client.login("admin", "admin")
        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest(username = "dev", password = "secret", role = UserRole.DEVELOPER))
        }

        val devClient = createClient {
            install(ContentNegotiation) {
                json()
            }
            install(HttpCookies)
        }
        devClient.login("dev", "secret")

        assertThat(devClient.get("/users").status).isEqualTo(HttpStatusCode.Forbidden)
        assertThat(devClient.get("/apiKey").status).isEqualTo(HttpStatusCode.Forbidden)
        assertThat(devClient.get("/projects").status).isEqualTo(HttpStatusCode.OK)

        val createProject = devClient.post("/projects") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("projectId" to "p1", "name" to "Project"))
        }
        assertThat(createProject.status).isEqualTo(HttpStatusCode.Forbidden)
        assertThat(devClient.delete("/projects/p1").status).isEqualTo(HttpStatusCode.Forbidden)
    }

    @Test
    fun `last admin cannot be deleted or demoted`() = authTestApplication { client ->
        client.login("admin", "admin")

        assertThat(client.delete("/users/admin").status).isEqualTo(HttpStatusCode.Conflict)

        val demote = client.put("/users/admin") {
            contentType(ContentType.Application.Json)
            setBody(UpdateUserRequest(role = UserRole.DEVELOPER))
        }
        assertThat(demote.status).isEqualTo(HttpStatusCode.Conflict)

        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest(username = "admin2", password = "secret", role = UserRole.ADMIN))
        }

        val demoteWithBackup = client.put("/users/admin") {
            contentType(ContentType.Application.Json)
            setBody(UpdateUserRequest(role = UserRole.DEVELOPER))
        }
        assertThat(demoteWithBackup.status).isEqualTo(HttpStatusCode.OK)
    }

    @Test
    fun `user can change own password with correct current password`() = authTestApplication { adminClient ->
        adminClient.login("admin", "admin")
        adminClient.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest(username = "dev", password = "secret", role = UserRole.DEVELOPER))
        }

        val devUserClient = createClient {
            install(ContentNegotiation) {
                json()
            }
            install(HttpCookies)
        }
        devUserClient.login("dev", "secret")

        val wrongCurrent = devUserClient.put("/me/password") {
            contentType(ContentType.Application.Json)
            setBody(ChangePasswordRequest(currentPassword = "wrong", newPassword = "newSecret"))
        }
        assertThat(wrongCurrent.status).isEqualTo(HttpStatusCode.Forbidden)

        val changed = devUserClient.put("/me/password") {
            contentType(ContentType.Application.Json)
            setBody(ChangePasswordRequest(currentPassword = "secret", newPassword = "newSecret"))
        }
        assertThat(changed.status).isEqualTo(HttpStatusCode.OK)

        assertThat(devUserClient.login("dev", "secret").status).isEqualTo(HttpStatusCode.Unauthorized)
        assertThat(devUserClient.login("dev", "newSecret").status).isEqualTo(HttpStatusCode.OK)
    }

    @Test
    fun `admin can reset a user password`() = authTestApplication { client ->
        client.login("admin", "admin")
        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest(username = "dev", password = "secret", role = UserRole.DEVELOPER))
        }

        val reset = client.put("/users/dev") {
            contentType(ContentType.Application.Json)
            setBody(UpdateUserRequest(password = "resetByAdmin"))
        }
        assertThat(reset.status).isEqualTo(HttpStatusCode.OK)

        val devUserClient = createClient {
            install(ContentNegotiation) {
                json()
            }
            install(HttpCookies)
        }
        assertThat(devUserClient.login("dev", "resetByAdmin").status).isEqualTo(HttpStatusCode.OK)
    }
}