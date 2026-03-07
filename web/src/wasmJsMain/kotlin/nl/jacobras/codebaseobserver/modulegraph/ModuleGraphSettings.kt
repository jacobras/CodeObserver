package nl.jacobras.codebaseobserver.modulegraph

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.gabrieldrn.carbon.textinput.TextInput
import io.ktor.client.HttpClient
import nl.jacobras.codebaseobserver.ui.loading.ProgressIndicator
import nl.jacobras.codebaseobserver.ui.table.DataTable

@Composable
internal fun ModuleRules(
    client: HttpClient,
    projectId: String
) {
    val viewModel = viewModel { ModuleRulesViewModel(client) }
    val settings by viewModel.settings.collectAsState(emptyList())
    val isLoading by viewModel.isLoading.collectAsState(false)
    val loadingError by viewModel.loadingError.collectAsState("")
    val updateError by viewModel.updateError.collectAsState("")
    var editingId by remember { mutableStateOf<Int?>(null) }
    var formType by remember { mutableStateOf("deprecatedModule") }
    var formData by remember { mutableStateOf("") }

    val isEditing = editingId != null

    LaunchedEffect(projectId) { viewModel.setProjectId(projectId) }

    if (isLoading || loadingError.isNotEmpty() || updateError.isNotEmpty()) {
        ProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            loading = isLoading,
            error = updateError.ifEmpty { loadingError },
            onRetry = if (loadingError.isNotEmpty()) {
                { viewModel.refresh() }
            } else {
                null
            }
        )
        return
    }

    fun clearForm() {
        editingId = null
        formType = "deprecatedModule"
        formData = ""
    }

    Column(Modifier.fillMaxWidth()) {
        Dropdown(
            label = "Type",
            placeholder = "Select type",
            options = settingTypes,
            selectedOption = formType,
            onOptionSelected = { formType = it },
            state = DropdownInteractiveState.Enabled
        )
        Spacer(Modifier.height(8.dp))
        TextInput(
            label = "Data",
            value = formData,
            onValueChange = { formData = it },
            placeholderText = if (formType == "deprecatedModule") {
                "util:deprecated"
            } else {
                "* -> moduleB  or  moduleA -> *"
            }
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                label = if (isEditing) "Update" else "Add setting",
                buttonType = ButtonType.Primary,
                buttonSize = ButtonSize.Small,
                isEnabled = formData.trim().isNotEmpty(),
                onClick = {
                    viewModel.save(editingId, formType, formData.trim())
                    clearForm()
                }
            )
            Button(
                label = "Clear",
                buttonType = ButtonType.Tertiary,
                buttonSize = ButtonSize.Small,
                isEnabled = formData.isNotEmpty() || isEditing,
                onClick = { clearForm() }
            )
        }
        Spacer(Modifier.height(20.dp))

        if (settings.isEmpty()) {
            BasicText(
                text = "No settings yet. Add one above.",
                style = Carbon.typography.body02
            )
        } else {
            DataTable(
                columnHeadings = listOf("Type", "Data", "Actions"),
                rowCount = settings.size,
                cellContent = { rowIndex, columnIndex, modifier ->
                    val setting = settings[rowIndex]
                    when (columnIndex) {
                        0 -> SelectionContainer(modifier) {
                            BasicText(
                                text = setting.type,
                                style = Carbon.typography.bodyCompact01
                            )
                        }
                        1 -> SelectionContainer(modifier) {
                            BasicText(
                                text = setting.data,
                                style = Carbon.typography.code01
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
                                onClick = {
                                    editingId = setting.id
                                    formType = setting.type
                                    formData = setting.data
                                }
                            )
                            Button(
                                label = "Delete",
                                buttonType = ButtonType.GhostDanger,
                                buttonSize = ButtonSize.Small,
                                onClick = { viewModel.delete(setting.id) }
                            )
                        }
                    }
                }
            )
        }
    }
}

private val settingTypes = linkedMapOf(
    "deprecatedModule" to DropdownOption("deprecatedModule"),
    "forbiddenDependency" to DropdownOption("forbiddenDependency")
)