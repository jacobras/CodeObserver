package nl.jacobras.codeobserver.auth

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import nl.jacobras.codeobserver.dto.ChangePasswordRequest
import nl.jacobras.codeobserver.dto.LoginRequest
import nl.jacobras.codeobserver.dto.UserDto
import nl.jacobras.codeobserver.util.data.NetworkError

internal interface AuthDataSource {
    suspend fun me(): Result<UserDto, NetworkError>
    suspend fun login(username: String, password: String): Result<UserDto, NetworkError>
    suspend fun logout(): Result<Unit, NetworkError>
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit, NetworkError>
}

internal class AuthDataSourceImpl(
    private val client: HttpClient
) : AuthDataSource {
    override suspend fun me(): Result<UserDto, NetworkError> {
        return runSuspendCatching {
            client.get("/me").body<UserDto>()
        }.mapError { it.toNetworkError() }
    }

    override suspend fun login(username: String, password: String): Result<UserDto, NetworkError> {
        Logger.i("Logging in as $username")
        return runSuspendCatching {
            client.post("/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(username = username, password = password))
            }.body<UserDto>()
        }.mapError {
            Logger.e(it) { "Failed to log in" }
            it.toNetworkError()
        }
    }

    override suspend fun logout(): Result<Unit, NetworkError> {
        Logger.i("Logging out")
        return runSuspendCatching {
            client.post("/logout")
            Unit
        }.mapError { it.toNetworkError() }
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Result<Unit, NetworkError> {
        Logger.i("Changing own password")
        return runSuspendCatching {
            client.put("/me/password") {
                contentType(ContentType.Application.Json)
                setBody(ChangePasswordRequest(currentPassword = currentPassword, newPassword = newPassword))
            }
            Unit
        }.mapError {
            Logger.e(it) { "Failed to change password" }
            it.toNetworkError()
        }
    }
}

internal fun Throwable.toNetworkError(): NetworkError {
    val status = (this as? ResponseException)?.response?.status
    return when (status) {
        HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> NetworkError.Unauthorized
        else -> NetworkError.UnknownError
    }
}