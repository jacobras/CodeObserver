package nl.jacobras.codebaseobserver.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.button.Button
import com.gabrieldrn.carbon.button.ButtonSize
import com.gabrieldrn.carbon.button.ButtonType
import com.gabrieldrn.carbon.foundation.color.CarbonLayer
import com.gabrieldrn.carbon.foundation.color.layerBackground
import com.gabrieldrn.carbon.textinput.TextInput
import nl.jacobras.codebaseobserver.dto.ProjectDto
import nl.jacobras.codebaseobserver.ui.table.DataTable

@Composable
internal fun SettingsScreen(
    projects: List<ProjectDto>,
    error: String?,
    onSaveProject: (projectId: String, name: String) -> Unit,
    onDeleteProject: (projectId: String) -> Unit
) {
    var editProjectId by remember { mutableStateOf("") }
    var editName by remember { mutableStateOf("") }

    fun clearForm() {
        editProjectId = ""
        editName = ""
    }

    val isEditing = projects.any { it.projectId == editProjectId.trim() }
    CarbonLayer {
        Column(
            modifier = Modifier
                .layerBackground()
                .padding(16.dp)
        ) {
            BasicText(
                text = "Settings",
                style = Carbon.typography.heading06
            )
            Spacer(Modifier.height(16.dp))

            TextInput(
                label = "Project ID",
                value = editProjectId,
                onValueChange = { editProjectId = it },
                placeholderText = "example: my-app"
            )
            Spacer(Modifier.height(8.dp))
            TextInput(
                label = "Display name",
                value = editName,
                onValueChange = { editName = it },
                placeholderText = "example: My App"
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    label = if (isEditing) "Update project" else "Add project",
                    buttonType = ButtonType.Primary,
                    buttonSize = ButtonSize.Small,
                    isEnabled = editProjectId.trim().isNotEmpty() && editName.trim().isNotEmpty(),
                    onClick = {
                        onSaveProject(editProjectId, editName)
                        clearForm()
                    }
                )
                Button(
                    label = "Clear",
                    buttonType = ButtonType.Tertiary,
                    buttonSize = ButtonSize.Small,
                    isEnabled = editProjectId.isNotEmpty() || editName.isNotEmpty(),
                    onClick = { clearForm() }
                )
            }

            Spacer(Modifier.height(20.dp))
            ProjectsTable(
                projects = projects,
                onEdit = { project ->
                    editProjectId = project.projectId
                    editName = project.name
                },
                onDelete = onDeleteProject
            )

            if (error != null) {
                Spacer(Modifier.height(12.dp))
                BasicText(
                    text = "Error: $error",
                    style = Carbon.typography.body02.copy(color = Carbon.theme.supportError)
                )
            }
        }
    }
}

@Composable
private fun ProjectsTable(
    projects: List<ProjectDto>,
    onEdit: (ProjectDto) -> Unit,
    onDelete: (projectId: String) -> Unit
) {
    if (projects.isEmpty()) {
        BasicText(
            text = "No projects yet. Add one above.",
            style = Carbon.typography.body02
        )
        return
    }

    DataTable(
        columnHeadings = listOf("Project ID", "Name", "Actions"),
        rowCount = projects.size,
        cellContent = { rowIndex, columnIndex, modifier ->
            val project = projects[rowIndex]
            when (columnIndex) {
                0 -> SelectionContainer(modifier) {
                    BasicText(
                        text = project.projectId,
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
                    Button(
                        label = "Edit",
                        buttonType = ButtonType.Ghost,
                        buttonSize = ButtonSize.Small,
                        onClick = { onEdit(project) }
                    )
                    Button(
                        label = "Delete",
                        buttonType = ButtonType.GhostDanger,
                        buttonSize = ButtonSize.Small,
                        onClick = { onDelete(project.projectId) }
                    )
                }
            }
        }
    )
}