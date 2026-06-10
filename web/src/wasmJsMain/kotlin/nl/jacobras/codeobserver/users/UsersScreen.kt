package nl.jacobras.codeobserver.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.button.Button
import com.gabrieldrn.carbon.button.ButtonSize
import com.gabrieldrn.carbon.button.ButtonType
import com.gabrieldrn.carbon.dropdown.Dropdown
import com.gabrieldrn.carbon.dropdown.base.DropdownInteractiveState
import com.gabrieldrn.carbon.dropdown.base.DropdownOption
import com.gabrieldrn.carbon.foundation.color.CarbonLayer
import com.gabrieldrn.carbon.foundation.color.layerBackground
import com.gabrieldrn.carbon.textinput.PasswordInput
import com.gabrieldrn.carbon.textinput.TextInput
import com.gabrieldrn.carbon.textinput.TextInputState
import nl.jacobras.codeobserver.di.RepositoryLocator
import nl.jacobras.codeobserver.dto.UserDto
import nl.jacobras.codeobserver.dto.UserRole
import nl.jacobras.codeobserver.util.data.RequestState
import nl.jacobras.codeobserver.util.ui.UiState
import nl.jacobras.codeobserver.util.ui.button.SmallProgressButton
import nl.jacobras.codeobserver.util.ui.dialog.DeleteDialog
import nl.jacobras.codeobserver.util.ui.progress.ProgressIndicator
import nl.jacobras.codeobserver.util.ui.table.DataTable

@Composable
internal fun UsersScreen() {
    val viewModel = viewModel { UsersScreenViewModel(RepositoryLocator.usersRepository) }
    val users by viewModel.users.collectAsState(emptyList())
    val apiKey by viewModel.apiKey.collectAsState(null)
    val state by viewModel.state.collectAsState(UiState())

    var editUsername by remember { mutableStateOf<String?>(null) }
    var formUsername by remember { mutableStateOf("") }
    var formPassword by remember { mutableStateOf("") }
    var formRole by remember { mutableStateOf(UserRole.DEVELOPER) }
    var passwordHidden by remember { mutableStateOf(true) }

    fun clearForm() {
        editUsername = null
        formUsername = ""
        formPassword = ""
        formRole = UserRole.DEVELOPER
    }

    val isEditing = editUsername != null
    CarbonLayer {
        Column(
            modifier = Modifier
                .layerBackground()
                .padding(16.dp)
        ) {
            when (val loading = state.loading) {
                is RequestState.Working -> {
                    ProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        loading = true
                    )
                }
                is RequestState.Error -> {
                    ProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        error = loading.type.name,
                        onRetry = { viewModel.refresh() }
                    )
                }
                RequestState.Idle -> Unit
            }

            BasicText(
                text = "Users",
                style = Carbon.typography.heading06
            )
            Spacer(Modifier.height(16.dp))

            TextInput(
                label = "Username",
                value = formUsername,
                onValueChange = { formUsername = it },
                placeholderText = "username",
                state = if (isEditing) TextInputState.Disabled else TextInputState.Enabled
            )
            Spacer(Modifier.height(8.dp))
            PasswordInput(
                label = if (isEditing) "New password (leave empty to keep current)" else "Password",
                value = formPassword,
                passwordHidden = passwordHidden,
                onValueChange = { formPassword = it },
                onPasswordHiddenChange = { passwordHidden = it }
            )
            Spacer(Modifier.height(8.dp))
            Dropdown(
                label = "Role",
                placeholder = "Select role",
                options = UserRole.entries.associateWith { DropdownOption(it.name.lowercase()) },
                selectedOption = formRole,
                onOptionSelected = { formRole = it },
                state = DropdownInteractiveState.Enabled
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val saving = state.saving
                val canSubmit = if (isEditing) {
                    true
                } else {
                    formUsername.isNotBlank() && formPassword.isNotEmpty()
                }

                SmallProgressButton(
                    label = if (isEditing) "Update user" else "Add user",
                    buttonType = ButtonType.Primary,
                    isEnabled = canSubmit,
                    loading = saving is RequestState.Working,
                    onClick = {
                        val editing = editUsername
                        if (editing != null) {
                            viewModel.updateUser(
                                username = editing,
                                role = formRole,
                                password = formPassword.ifEmpty { null },
                                onSuccess = { clearForm() }
                            )
                        } else {
                            viewModel.createUser(
                                username = formUsername.trim(),
                                password = formPassword,
                                role = formRole,
                                onSuccess = { clearForm() }
                            )
                        }
                    }
                )
                Button(
                    label = "Clear",
                    buttonType = ButtonType.Tertiary,
                    buttonSize = ButtonSize.Small,
                    isEnabled = isEditing || formUsername.isNotEmpty() || formPassword.isNotEmpty(),
                    onClick = { clearForm() }
                )
            }

            Spacer(Modifier.height(20.dp))
            UsersTable(
                users = users,
                deleting = state.deleting,
                onEdit = { user ->
                    editUsername = user.username
                    formUsername = user.username
                    formPassword = ""
                    formRole = user.role
                },
                onDelete = { viewModel.deleteUser(it) }
            )

            Spacer(Modifier.height(24.dp))
            ApiKeySection(apiKey = apiKey)
        }
    }
}

@Composable
private fun UsersTable(
    users: List<UserDto>,
    deleting: Map<String, RequestState>,
    onEdit: (UserDto) -> Unit,
    onDelete: (username: String) -> Unit
) {
    if (users.isEmpty()) {
        return
    }

    var requestDeleteUsername by remember { mutableStateOf<String?>(null) }
    requestDeleteUsername?.let { username ->
        DeleteDialog(
            message = "Are you sure you want to delete this user?",
            onCancel = { requestDeleteUsername = null },
            onDelete = {
                onDelete(username)
                requestDeleteUsername = null
            }
        )
    }

    DataTable(
        columnHeadings = listOf("Username", "Role", "Actions"),
        rowCount = users.size,
        cellContent = { rowIndex, columnIndex, modifier ->
            val user = users[rowIndex]
            when (columnIndex) {
                0 -> SelectionContainer(modifier) {
                    BasicText(
                        text = user.username,
                        style = Carbon.typography.bodyCompact01
                    )
                }
                1 -> BasicText(
                    text = user.role.name.lowercase(),
                    style = Carbon.typography.bodyCompact01,
                    modifier = modifier
                )
                2 -> Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = modifier
                ) {
                    val isDeleting = deleting[user.username] is RequestState.Working
                    Button(
                        label = "Edit",
                        buttonType = ButtonType.Ghost,
                        buttonSize = ButtonSize.Small,
                        isEnabled = !isDeleting,
                        onClick = { onEdit(user) }
                    )
                    SmallProgressButton(
                        label = "Delete",
                        buttonType = ButtonType.GhostDanger,
                        loading = isDeleting,
                        onClick = { requestDeleteUsername = user.username }
                    )
                }
            }
        }
    )
}

@Composable
private fun ApiKeySection(apiKey: String?) {
    BasicText(
        text = "CLI API key",
        style = Carbon.typography.heading03
    )
    Spacer(Modifier.height(8.dp))
    BasicText(
        text = "Pass this key to the CLI via --api-key or the CODEOBSERVER_API_KEY environment variable.",
        style = Carbon.typography.body01
    )
    Spacer(Modifier.height(8.dp))
    SelectionContainer {
        BasicText(
            text = apiKey ?: "Loading…",
            style = Carbon.typography.code01
        )
    }
}