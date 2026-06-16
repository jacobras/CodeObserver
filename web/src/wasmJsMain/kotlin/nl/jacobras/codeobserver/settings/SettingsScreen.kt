package nl.jacobras.codeobserver.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.button.Button
import com.gabrieldrn.carbon.button.ButtonSize
import com.gabrieldrn.carbon.button.ButtonType
import com.gabrieldrn.carbon.foundation.color.CarbonLayer
import com.gabrieldrn.carbon.foundation.color.layerBackground
import com.gabrieldrn.carbon.textinput.PasswordInput
import com.gabrieldrn.carbon.textinput.TextInput
import com.gabrieldrn.carbon.textinput.TextInputState
import nl.jacobras.codeobserver.auth.isCurrentUserAdmin
import nl.jacobras.codeobserver.dto.ProjectDto
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.codeobserver.util.data.RequestState
import nl.jacobras.codeobserver.util.ui.UiState
import nl.jacobras.codeobserver.util.ui.button.SmallProgressButton
import nl.jacobras.codeobserver.util.ui.dialog.DeleteDialog
import nl.jacobras.codeobserver.util.ui.progress.ProgressIndicator
import nl.jacobras.codeobserver.util.ui.table.DataTable
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SettingsScreen() {
    val viewModel = koinViewModel<SettingsScreenViewModel>()
    val projects by viewModel.projects.collectAsState(emptyList())
    val state by viewModel.state.collectAsState(UiState())
    val isAdmin = isCurrentUserAdmin()

    var editingProject by remember { mutableStateOf<ProjectDto?>(null) }
    var showForm by remember { mutableStateOf(false) }

    if (showForm) {
        ProjectFormDialog(
            project = editingProject,
            saving = state.saving,
            onSubmit = { projectId, name ->
                viewModel.saveProject(
                    projectId = projectId,
                    name = name,
                    onSuccess = {
                        showForm = false
                        editingProject = null
                    }
                )
            },
            onCancel = {
                showForm = false
                editingProject = null
            }
        )
    }

    CarbonLayer {
        Column(
            modifier = Modifier
                .layerBackground()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
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
                text = "Settings",
                style = Carbon.typography.heading06
            )
            Spacer(Modifier.height(16.dp))

            if (isAdmin) {
                Button(
                    label = "Add project",
                    buttonType = ButtonType.Primary,
                    buttonSize = ButtonSize.Small,
                    onClick = {
                        editingProject = null
                        showForm = true
                    }
                )
                Spacer(Modifier.height(16.dp))
            }
            // FIXME: this list is empty?!
            ProjectsTable(
                projects = projects,
                canEdit = isAdmin,
                deleting = state.deleting,
                onEdit = { project ->
                    editingProject = project
                    showForm = true
                },
                onDelete = { viewModel.deleteProject(it) }
            )

            Spacer(Modifier.height(24.dp))
            ChangePasswordSection()
        }
    }
}

@Composable
private fun ProjectFormDialog(
    project: ProjectDto?,
    saving: RequestState,
    onSubmit: (projectId: ProjectId, name: String) -> Unit,
    onCancel: () -> Unit
) {
    val isEditing = project != null
    var projectIdText by remember { mutableStateOf(project?.id?.value ?: "") }
    var name by remember { mutableStateOf(project?.name ?: "") }

    val canSubmit = projectIdText.trim().isNotEmpty() && name.trim().isNotEmpty()

    Dialog(
        onDismissRequest = onCancel,
        content = {
            CarbonLayer {
                Column(
                    modifier = Modifier
                        .layerBackground()
                        .padding(16.dp)
                        .width(480.dp)
                ) {
                    BasicText(
                        text = if (isEditing) "Edit project" else "Add project",
                        style = Carbon.typography.heading03.copy(color = Carbon.theme.textPrimary)
                    )
                    Spacer(Modifier.height(16.dp))
                    TextInput(
                        label = "Project ID",
                        value = projectIdText,
                        onValueChange = { projectIdText = it },
                        placeholderText = "my-app",
                        state = if (isEditing) TextInputState.Disabled else TextInputState.Enabled
                    )
                    Spacer(Modifier.height(8.dp))
                    TextInput(
                        label = "Display name",
                        value = name,
                        onValueChange = { name = it },
                        placeholderText = "My App"
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            label = "Cancel",
                            buttonType = ButtonType.Secondary,
                            buttonSize = ButtonSize.Small,
                            onClick = onCancel
                        )
                        SmallProgressButton(
                            label = if (isEditing) "Update project" else "Add project",
                            buttonType = ButtonType.Primary,
                            isEnabled = canSubmit,
                            loading = saving is RequestState.Working,
                            onClick = { onSubmit(ProjectId(projectIdText.trim()), name.trim()) }
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun ChangePasswordSection() {
    val viewModel = koinViewModel<ChangePasswordViewModel>()
    val changing by viewModel.changing.collectAsState()

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var passwordHidden by remember { mutableStateOf(true) }

    BasicText(
        text = "Change password",
        style = Carbon.typography.heading03
    )
    Spacer(Modifier.height(12.dp))
    PasswordInput(
        label = "Current password",
        value = currentPassword,
        passwordHidden = passwordHidden,
        onValueChange = { currentPassword = it },
        onPasswordHiddenChange = { passwordHidden = it }
    )
    Spacer(Modifier.height(8.dp))
    PasswordInput(
        label = "New password",
        value = newPassword,
        passwordHidden = passwordHidden,
        onValueChange = { newPassword = it },
        onPasswordHiddenChange = { passwordHidden = it }
    )
    Spacer(Modifier.height(12.dp))
    SmallProgressButton(
        label = "Change password",
        buttonType = ButtonType.Primary,
        isEnabled = currentPassword.isNotEmpty() && newPassword.isNotEmpty() && !changing,
        loading = changing,
        onClick = {
            viewModel.changePassword(
                currentPassword = currentPassword,
                newPassword = newPassword,
                onSuccess = {
                    currentPassword = ""
                    newPassword = ""
                }
            )
        }
    )
}

@Composable
private fun ProjectsTable(
    projects: List<ProjectDto>,
    canEdit: Boolean,
    deleting: Map<ProjectId, RequestState>,
    onEdit: (ProjectDto) -> Unit,
    onDelete: (projectId: ProjectId) -> Unit
) {
    if (projects.isEmpty()) {
        BasicText(
            text = if (canEdit) "No projects yet. Add one to get started." else "No projects yet.",
            style = Carbon.typography.body02
        )
        return
    }

    var requestDeleteProjectId by remember { mutableStateOf<ProjectId?>(null) }
    requestDeleteProjectId?.let { projectId ->
        DeleteDialog(
            message = "Are you sure you want to delete this project?",
            onCancel = { requestDeleteProjectId = null },
            onDelete = {
                onDelete(projectId)
                requestDeleteProjectId = null
            }
        )
    }

    DataTable(
        modifier = Modifier.height(400.dp),
        columnHeadings = if (canEdit) {
            listOf("Project ID", "Name", "Actions")
        } else {
            listOf("Project ID", "Name")
        },
        rowCount = projects.size,
        cellContent = { rowIndex, columnIndex, modifier ->
            val project = projects[rowIndex]
            when (columnIndex) {
                0 -> SelectionContainer(modifier) {
                    BasicText(
                        text = project.id.value,
                        style = Carbon.typography.code01
                    )
                }
                1 -> SelectionContainer(modifier) {
                    BasicText(
                        text = project.name,
                        style = Carbon.typography.bodyCompact01
                    )
                }
                2 -> Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = modifier
                ) {
                    val isDeleting = deleting[project.id] is RequestState.Working
                    Button(
                        label = "Edit",
                        buttonType = ButtonType.Ghost,
                        buttonSize = ButtonSize.Small,
                        isEnabled = !isDeleting,
                        onClick = { onEdit(project) }
                    )
                    SmallProgressButton(
                        label = "Delete",
                        buttonType = ButtonType.GhostDanger,
                        loading = isDeleting,
                        onClick = { requestDeleteProjectId = project.id }
                    )
                }
            }
        }
    )
}