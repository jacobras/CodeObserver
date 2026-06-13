package nl.jacobras.codeobserver.di.demo

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import nl.jacobras.codeobserver.dto.UserDto
import nl.jacobras.codeobserver.dto.UserRole
import nl.jacobras.codeobserver.users.UsersDataSource
import nl.jacobras.codeobserver.util.data.NetworkError

internal class DemoUsersDataSource : UsersDataSource {
    private val users = mutableListOf(
        UserDto(username = "demo", role = UserRole.Admin),
        UserDto(username = "developer", role = UserRole.Developer)
    )

    override suspend fun fetch(): Result<List<UserDto>, NetworkError> = Ok(users.toList())

    override suspend fun create(
        username: String,
        password: String,
        role: UserRole
    ): Result<Unit, NetworkError> {
        if (users.any { it.username == username }) {
            return Err(NetworkError.Conflict)
        }
        users.add(UserDto(username = username, role = role))
        return Ok(Unit)
    }

    override suspend fun update(
        username: String,
        role: UserRole?,
        password: String?
    ): Result<Unit, NetworkError> {
        val index = users.indexOfFirst { it.username == username }
        if (index < 0) {
            return Err(NetworkError.UnknownError)
        }
        val demotesLastAdmin = role != null && role != UserRole.Admin &&
            users[index].role == UserRole.Admin &&
            users.count { it.role == UserRole.Admin } <= 1
        if (demotesLastAdmin) {
            return Err(NetworkError.Conflict)
        }
        if (role != null) {
            users[index] = users[index].copy(role = role)
        }
        return Ok(Unit)
    }

    override suspend fun delete(username: String): Result<Unit, NetworkError> {
        val target = users.firstOrNull { it.username == username }
            ?: return Err(NetworkError.UnknownError)
        if (target.role == UserRole.Admin && users.count { it.role == UserRole.Admin } <= 1) {
            return Err(NetworkError.Conflict)
        }
        users.remove(target)
        return Ok(Unit)
    }

    override suspend fun fetchApiKey(): Result<String, NetworkError> = Ok("demo-api-key")
}