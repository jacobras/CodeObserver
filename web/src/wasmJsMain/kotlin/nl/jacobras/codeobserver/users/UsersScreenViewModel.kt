package nl.jacobras.codeobserver.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrieldrn.carbon.notification.NotificationStatus
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import nl.jacobras.codeobserver.dto.UserRole
import nl.jacobras.codeobserver.util.data.NetworkError
import nl.jacobras.codeobserver.util.ui.UiState
import nl.jacobras.codeobserver.util.ui.notification.Notifier

internal class UsersScreenViewModel(
    private val usersRepository: UsersRepository
) : ViewModel() {

    val state = combine(
        usersRepository.loadingState,
        usersRepository.savingState,
        usersRepository.deletingState
    ) { loading, saving, deleting ->
        UiState(loading = loading, saving = saving, deleting = deleting)
    }
    val users = usersRepository.users
    val apiKey = usersRepository.apiKey

    init {
        refresh()
        viewModelScope.launch {
            usersRepository.refreshApiKey()
        }
    }

    fun refresh() = viewModelScope.launch {
        usersRepository.refresh()
            .onErr {
                Notifier.show(
                    title = "Error loading users",
                    message = "Failed to load users",
                    status = NotificationStatus.Error
                )
            }
    }

    fun createUser(username: String, password: String, role: UserRole, onSuccess: () -> Unit) =
        viewModelScope.launch {
            usersRepository.create(username, password, role).onOk {
                Notifier.show(
                    title = "User '$username' created",
                    status = NotificationStatus.Success
                )
                onSuccess()
            }.onErr { error ->
                Notifier.show(
                    title = "Error creating user",
                    message = when (error) {
                        NetworkError.Conflict -> "Username '$username' already exists"
                        else -> "Due to $error"
                    },
                    status = NotificationStatus.Error
                )
            }
        }

    fun updateUser(username: String, role: UserRole?, password: String?, onSuccess: () -> Unit) =
        viewModelScope.launch {
            usersRepository.update(username, role, password).onOk {
                Notifier.show(
                    title = "User '$username' updated",
                    status = NotificationStatus.Success
                )
                onSuccess()
            }.onErr { error ->
                Notifier.show(
                    title = "Error updating user",
                    message = when (error) {
                        NetworkError.Conflict -> "There must always be at least one admin"
                        else -> "Due to $error"
                    },
                    status = NotificationStatus.Error
                )
            }
        }

    fun deleteUser(username: String) = viewModelScope.launch {
        usersRepository.delete(username).onOk {
            Notifier.show(
                title = "User '$username' deleted",
                status = NotificationStatus.Success
            )
        }.onErr { error ->
            Notifier.show(
                title = "Error deleting user",
                message = when (error) {
                    NetworkError.Conflict -> "There must always be at least one admin"
                    else -> "Due to $error"
                },
                status = NotificationStatus.Error
            )
        }
    }
}