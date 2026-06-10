package nl.jacobras.codeobserver.auth

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import nl.jacobras.codeobserver.dto.UserDto
import nl.jacobras.codeobserver.util.data.NetworkError

internal sealed class AuthState {

    data object Pending : AuthState()

    data object LoggedOut : AuthState()

    data class LoggedIn(val user: UserDto) : AuthState()
}

@OptIn(DelicateCoroutinesApi::class)
internal class AuthRepository(
    private val dataSource: AuthDataSource
) {
    val authState = MutableStateFlow<AuthState>(AuthState.Pending)

    init {
        GlobalScope.launch {
            dataSource.me()
                .onOk { authState.value = AuthState.LoggedIn(it) }
                .onErr { authState.value = AuthState.LoggedOut }
        }
        GlobalScope.launch {
            AuthEvents.unauthorized.collect {
                authState.value = AuthState.LoggedOut
            }
        }
    }

    suspend fun login(username: String, password: String): Result<UserDto, NetworkError> {
        return dataSource.login(username, password)
            .onOk { authState.value = AuthState.LoggedIn(it) }
    }

    suspend fun logout() {
        dataSource.logout()
        authState.value = AuthState.LoggedOut
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit, NetworkError> {
        return dataSource.changePassword(currentPassword, newPassword)
    }
}