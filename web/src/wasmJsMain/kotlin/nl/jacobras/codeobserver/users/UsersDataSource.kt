package nl.jacobras.codeobserver.users

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import nl.jacobras.codeobserver.dto.ApiKeyDto
import nl.jacobras.codeobserver.dto.CreateUserRequest
import nl.jacobras.codeobserver.dto.UpdateUserRequest
import nl.jacobras.codeobserver.dto.UserDto
import nl.jacobras.codeobserver.dto.UserRole
import nl.jacobras.codeobserver.util.data.NetworkError

internal interface UsersDataSource {
    suspend fun fetch(): Result<List<UserDto>, NetworkError>
    suspend fun create(username: String, password: String, role: UserRole): Result<Unit, NetworkError>
    suspend fun update(username: String, role: UserRole?, password: String?): Result<Unit, NetworkError>
    suspend fun delete(username: String): Result<Unit, NetworkError>
    suspend fun fetchApiKey(): Result<String, NetworkError>
}

internal class UsersDataSourceImpl(
    private val client: HttpClient
) : UsersDataSource {
    override suspend fun fetch(): Result<List<UserDto>, NetworkError> {
        Logger.i("Fetching users")
        return runSuspendCatching {
            client.get("/users").body<List<UserDto>>()
        }.mapError {
            Logger.e(it) { "Failed to fetch users" }
            it.toUsersError()
        }
    }

    override suspend fun create(
        username: String,
        password: String,
        role: UserRole
    ): Result<Unit, NetworkError> {
        Logger.i("Creating user: $username")
        return runSuspendCatching {
            client.post("/users") {
                contentType(ContentType.Application.Json)
                setBody(CreateUserRequest(username = username, password = password, role = role))
            }
            Unit
        }.mapError {
            Logger.e(it) { "Failed to create user" }
            it.toUsersError()
        }
    }

    override suspend fun update(
        username: String,
        role: UserRole?,
        password: String?
    ): Result<Unit, NetworkError> {
        Logger.i("Updating user: $username")
        return runSuspendCatching {
            client.put("/users/$username") {
                contentType(ContentType.Application.Json)
                setBody(UpdateUserRequest(role = role, password = password))
            }
            Unit
        }.mapError {
            Logger.e(it) { "Failed to update user" }
            it.toUsersError()
        }
    }

    override suspend fun delete(username: String): Result<Unit, NetworkError> {
        Logger.i("Deleting user: $username")
        return runSuspendCatching {
            client.delete("/users/$username")
            Unit
        }.mapError {
            Logger.e(it) { "Failed to delete user" }
            it.toUsersError()
        }
    }

    override suspend fun fetchApiKey(): Result<String, NetworkError> {
        return runSuspendCatching {
            client.get("/apiKey").body<ApiKeyDto>().key
        }.mapError {
            Logger.e(it) { "Failed to fetch API key" }
            it.toUsersError()
        }
    }
}

private fun Throwable.toUsersError(): NetworkError {
    return when ((this as? ResponseException)?.response?.status) {
        HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> NetworkError.Unauthorized
        HttpStatusCode.Conflict -> NetworkError.Conflict
        else -> NetworkError.UnknownError
    }
}