package nl.jacobras.codeobserver.di.demo

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import nl.jacobras.codeobserver.auth.AuthDataSource
import nl.jacobras.codeobserver.dto.UserDto
import nl.jacobras.codeobserver.dto.UserRole
import nl.jacobras.codeobserver.util.data.NetworkError

/**
 * Always-logged-in admin, so the public demo works without authentication.
 */
internal class DemoAuthDataSource : AuthDataSource {
    private val demoUser = UserDto(username = "demo", role = UserRole.ADMIN)

    override suspend fun me(): Result<UserDto, NetworkError> = Ok(demoUser)

    override suspend fun login(username: String, password: String): Result<UserDto, NetworkError> = Ok(demoUser)

    override suspend fun logout(): Result<Unit, NetworkError> = Ok(Unit)

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Result<Unit, NetworkError> = Ok(Unit)
}