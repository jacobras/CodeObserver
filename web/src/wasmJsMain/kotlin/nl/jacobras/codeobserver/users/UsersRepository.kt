package nl.jacobras.codeobserver.users

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import nl.jacobras.codeobserver.dto.UserDto
import nl.jacobras.codeobserver.dto.UserRole
import nl.jacobras.codeobserver.util.data.NetworkError
import nl.jacobras.codeobserver.util.data.RequestState

internal class UsersRepository(
    private val dataSource: UsersDataSource
) {
    val users = MutableStateFlow(emptyList<UserDto>())
    val apiKey = MutableStateFlow<String?>(null)

    val loadingState = MutableStateFlow<RequestState>(RequestState.Idle)
    val savingState = MutableStateFlow<RequestState>(RequestState.Idle)
    val deletingState = MutableStateFlow<Map<String, RequestState>>(emptyMap())

    suspend fun refresh(): Result<List<UserDto>, NetworkError> {
        loadingState.update { RequestState.Working }
        return dataSource.fetch()
            .onOk { newValue ->
                users.value = newValue
                loadingState.update { RequestState.Idle }
            }
            .onErr { error ->
                loadingState.update { RequestState.Error(error) }
            }
    }

    suspend fun refreshApiKey() {
        dataSource.fetchApiKey()
            .onOk { apiKey.value = it }
    }

    suspend fun create(username: String, password: String, role: UserRole): Result<Unit, NetworkError> {
        savingState.update { RequestState.Working }
        return dataSource.create(username, password, role)
            .onOk {
                savingState.update { RequestState.Idle }
                refresh()
            }
            .onErr { error ->
                savingState.update { RequestState.Error(error) }
            }
    }

    suspend fun update(username: String, role: UserRole?, password: String?): Result<Unit, NetworkError> {
        savingState.update { RequestState.Working }
        return dataSource.update(username, role, password)
            .onOk {
                savingState.update { RequestState.Idle }
                refresh()
            }
            .onErr { error ->
                savingState.update { RequestState.Error(error) }
            }
    }

    suspend fun delete(username: String): Result<Unit, NetworkError> {
        deletingState.update { it + mapOf(username to RequestState.Working) }
        return dataSource.delete(username)
            .onOk {
                deletingState.update { it - username }
                Logger.i { "User $username deleted" }
                refresh()
            }
            .onErr { error ->
                deletingState.update { it + mapOf(username to RequestState.Error(error)) }
            }
    }

    fun clearCache() {
        users.update { emptyList() }
        apiKey.update { null }
    }
}