package nl.jacobras.codeobserver.settings

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
import com.gabrieldrn.carbon.foundation.color.CarbonLayer
import com.gabrieldrn.carbon.foundation.color.layerBackground
import com.gabrieldrn.carbon.textinput.TextInput
import nl.jacobras.codeobserver.di.RepositoryLocator
import nl.jacobras.codeobserver.dto.ProjectDto
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.codeobserver.util.data.RequestState
import nl.jacobras.codeobserver.util.ui.UiState
import nl.jacobras.codeobserver.util.ui.button.SmallProgressButton
import nl.jacobras.codeobserver.util.ui.dialog.DeleteDialog
import nl.jacobras.codeobserver.util.ui.loading.ProgressIndicator
import nl.jacobras.codeobserver.util.ui.table.DataTable

@Composable
internal fun SettingsScreen() {
    val viewModel = viewModel { SettingsScreenViewModel(RepositoryLocator.projectRepository) }
    val projects by viewModel.projects.collectAsState(emptyList())
    val state by viewModel.state.collectAsState(UiState())

    var editProjectId by remember { mutableStateOf<ProjectId?>(null) }
    var editName by remember { mutableStateOf("") }

    fun clearForm() {
        editProjectId = null
        editName = ""
    }

    val isEditing = projects.any { it.id == editProjectId }
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
                text = "Settings",
                style = Carbon.typography.heading06
            )
            Spacer(Modifier.height(16.dp))

            TextInput(
                label = "Project ID",
                value = editProjectId?.value ?: "",
                onValueChange = { newValue ->
                    editProjectId = if (newValue.isNotBlank()) {
                        ProjectId(newValue)
                    } else {
                        null
                    }
                },
                placeholderText = "my-app"
            )
            Spacer(Modifier.height(8.dp))
            TextInput(
                label = "Display name",
                value = editName,
                onValueChange = { editName = it },
                placeholderText = "My App"
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val saving = state.saving

                SmallProgressButton(
                    label = if (isEditing) "Update project" else "Add project",
                    buttonType = ButtonType.Primary,
                    isEnabled = editProjectId != null && editName.trim().isNotEmpty(),
                    loading = saving is RequestState.Working,
                    onClick = {
                        val id = editProjectId ?: return@SmallProgressButton
                        viewModel.saveProject(
                            projectId = id,
                            name = editName.trim(),
                            onSuccess = { clearForm() }
                        )
                    }
                )
                Button(
                    label = "Clear",
                    buttonType = ButtonType.Tertiary,
                    buttonSize = ButtonSize.Small,
                    isEnabled = editProjectId != null || editName.isNotEmpty(),
                    onClick = { clearForm() }
                )
            }

            Spacer(Modifier.height(20.dp))
            ProjectsTable(
                projects = projects,
                deleting = state.deleting,
                onEdit = { project ->
                    editProjectId = project.id
                    editName = project.name
                },
                onDelete = { viewModel.deleteProject(it) }
            )
        }
    }
}

@Composable
private fun ProjectsTable(
    projects: List<ProjectDto>,
    deleting: Map<ProjectId, RequestState>,
    onEdit: (ProjectDto) -> Unit,
    onDelete: (projectId: ProjectId) -> Unit
) {
    if (projects.isEmpty()) {
        BasicText(
            text = "No projects yet. Add one above.",
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
        columnHeadings = listOf("Project ID", "Name", "Actions"),
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