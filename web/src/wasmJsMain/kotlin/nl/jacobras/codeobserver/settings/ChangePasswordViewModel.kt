package nl.jacobras.codeobserver.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrieldrn.carbon.notification.NotificationStatus
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import nl.jacobras.codeobserver.auth.AuthRepository
import nl.jacobras.codeobserver.util.data.NetworkError
import nl.jacobras.codeobserver.util.ui.notification.Notifier

internal class ChangePasswordViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val changing = MutableStateFlow(false)

    fun changePassword(currentPassword: String, newPassword: String, onSuccess: () -> Unit) =
        viewModelScope.launch {
            changing.value = true
            authRepository.changePassword(currentPassword, newPassword)
                .onOk {
                    Notifier.show(
                        title = "Password changed",
                        status = NotificationStatus.Success
                    )
                    onSuccess()
                }
                .onErr { error ->
                    Notifier.show(
                        title = "Error changing password",
                        message = when (error) {
                            NetworkError.Unauthorized -> "Current password is incorrect"
                            else -> "Due to $error"
                        },
                        status = NotificationStatus.Error
                    )
                }
            changing.value = false
        }
}