package nl.jacobras.codeobserver.dashboard.modulegraph

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.gabrieldrn.carbon.textinput.TextInput
import nl.jacobras.codeobserver.di.RepositoryLocator
import nl.jacobras.codeobserver.dto.ModuleGraphSettingId
import nl.jacobras.codeobserver.util.data.RequestState
import nl.jacobras.codeobserver.util.ui.UiState
import nl.jacobras.codeobserver.util.ui.dialog.DeleteDialog
import nl.jacobras.codeobserver.util.ui.progress.ProgressIndicator
import nl.jacobras.codeobserver.util.ui.table.DataTable

@Composable
internal fun ModuleRules() {
    val viewModel = viewModel {
        ModuleRulesViewModel(
            moduleGraphSettingsRepository = RepositoryLocator.moduleGraphSettingsRepository,
            projectRepository = RepositoryLocator.projectRepository
        )
    }
    val settings by viewModel.settings.collectAsState(emptyList())
    val state by viewModel.uiState.collectAsState(UiState())
    var editingId by remember { mutableStateOf<ModuleGraphSettingId?>(null) }
    var formType by remember { mutableStateOf("deprecatedModule") }
    var formData by remember { mutableStateOf("") }

    val isEditing = editingId != null

    val isLoading = state.loading is RequestState.Working
    val loadingError = (state.loading as? RequestState.Error)?.type?.name ?: ""
    val updateError = (state.saving as? RequestState.Error)?.type?.name
        ?: state.deleting.values.filterIsInstance<RequestState.Error>().firstOrNull()?.type?.name
        ?: ""

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
            var requestDeleteId by remember { mutableStateOf<ModuleGraphSettingId?>(null) }
            requestDeleteId?.let {
                DeleteDialog(
                    message = "Are you sure you want to delete this setting?",
                    onCancel = { requestDeleteId = null },
                    onDelete = {
                        viewModel.delete(it)
                        requestDeleteId = null
                    }
                )
            }

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
                                onClick = { requestDeleteId = setting.id }
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