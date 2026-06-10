package nl.jacobras.codeobserver.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onErr
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import nl.jacobras.codeobserver.util.data.NetworkError

internal class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val loggingIn = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)

    fun login(username: String, password: String) = viewModelScope.launch {
        loggingIn.value = true
        error.value = null
        authRepository.login(username, password)
            .onErr {
                error.value = when (it) {
                    NetworkError.Unauthorized -> "Invalid username or password"
                    else -> "Login failed, please try again"
                }
            }
        loggingIn.value = false
    }
}