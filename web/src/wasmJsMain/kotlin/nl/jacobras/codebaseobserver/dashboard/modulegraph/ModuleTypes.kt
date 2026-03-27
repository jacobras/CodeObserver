package nl.jacobras.codebaseobserver.dashboard.modulegraph

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
import nl.jacobras.codebaseobserver.di.RepositoryLocator
import nl.jacobras.codebaseobserver.util.data.RequestState
import nl.jacobras.codebaseobserver.util.ui.UiState
import nl.jacobras.codebaseobserver.util.ui.dialog.DeleteDialog
import nl.jacobras.codebaseobserver.util.ui.loading.ProgressIndicator
import nl.jacobras.codebaseobserver.util.ui.table.DataTable

@Composable
internal fun ModuleTypes() {
    val viewModel = viewModel {
        ModuleTypesViewModel(
            moduleTypeIdentifiersRepository = RepositoryLocator.moduleTypeIdentifiersRepository,
            projectRepository = RepositoryLocator.projectRepository
        )
    }
    val typeIdentifiers by viewModel.moduleTypeIdentifiers.collectAsState(emptyList())
    val state by viewModel.uiState.collectAsState(UiState())
    var editingId by remember { mutableStateOf<Int?>(null) }
    var formTypeName by remember { mutableStateOf("") }
    var formPlugin by remember { mutableStateOf("") }
    var formOrder by remember { mutableStateOf("0") }
    var formColor by remember { mutableStateOf("") }

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
        formTypeName = ""
        formPlugin = ""
        formOrder = "0"
        formColor = ""
    }

    val isFormValid =
        formTypeName.trim().isNotEmpty() && formPlugin.trim().isNotEmpty() && formColor.trim().isNotEmpty()

    Column(Modifier.fillMaxWidth()) {
        TextInput(
            label = "Name",
            value = formTypeName,
            onValueChange = { formTypeName = it },
            placeholderText = "android"
        )
        Spacer(Modifier.height(8.dp))
        TextInput(
            label = "Identifying plugin",
            value = formPlugin,
            onValueChange = { formPlugin = it },
            placeholderText = "libs.plugins.androidApplication or kotlin(\"multiplatform\")"
        )
        Spacer(Modifier.height(8.dp))
        TextInput(
            label = "Order",
            value = formOrder,
            onValueChange = { formOrder = it },
            placeholderText = "0"
        )
        Spacer(Modifier.height(8.dp))
        Dropdown(
            label = "Color",
            placeholder = "Select color",
            options = ModuleColors.entries.associate { it.hex to DropdownOption(value = it.name) },
            selectedOption = formColor,
            onOptionSelected = { formColor = it },
            state = DropdownInteractiveState.Enabled
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                label = if (isEditing) "Update" else "Add identifier",
                buttonType = ButtonType.Primary,
                buttonSize = ButtonSize.Small,
                isEnabled = isFormValid,
                onClick = {
                    viewModel.save(
                        id = editingId,
                        typeName = formTypeName.trim(),
                        plugin = formPlugin.trim(),
                        order = formOrder.trim().toIntOrNull() ?: 0,
                        color = formColor.trim()
                    )
                    clearForm()
                }
            )
            Button(
                label = "Clear",
                buttonType = ButtonType.Tertiary,
                buttonSize = ButtonSize.Small,
                isEnabled = formTypeName.isNotEmpty() || formPlugin.isNotEmpty() || formColor.isNotEmpty() || isEditing,
                onClick = { clearForm() }
            )
        }
        Spacer(Modifier.height(20.dp))

        if (typeIdentifiers.isEmpty()) {
            BasicText(
                text = "No module identifiers yet. Add one above.",
                style = Carbon.typography.body02
            )
        } else {
            var requestDeleteId by remember { mutableStateOf<Int?>(null) }
            if (requestDeleteId != null) {
                DeleteDialog(
                    message = "Are you sure you want to delete this identifier?",
                    onCancel = { requestDeleteId = null },
                    onDelete = {
                        viewModel.delete(requestDeleteId!!)
                        requestDeleteId = null
                    }
                )
            }

            DataTable(
                columnHeadings = listOf("Name", "Plugin", "Order", "Color", "Actions"),
                rowCount = typeIdentifiers.size,
                cellContent = { rowIndex, columnIndex, modifier ->
                    val identifier = typeIdentifiers[rowIndex]
                    when (columnIndex) {
                        0 -> SelectionContainer(modifier) {
                            BasicText(
                                text = identifier.typeName,
                                style = Carbon.typography.bodyCompact01
                            )
                        }
                        1 -> SelectionContainer(modifier) {
                            BasicText(
                                text = identifier.plugin,
                                style = Carbon.typography.code01
                            )
                        }
                        2 -> SelectionContainer(modifier) {
                            BasicText(
                                text = identifier.order.toString(),
                                style = Carbon.typography.bodyCompact01
                            )
                        }
                        3 -> SelectionContainer(modifier) {
                            BasicText(
                                text = ModuleColors.fromHex(identifier.color)?.name ?: "Unknown",
                                style = Carbon.typography.code01
                            )
                        }
                        4 -> Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = modifier
                        ) {
                            Button(
                                label = "Edit",
                                buttonType = ButtonType.Ghost,
                                buttonSize = ButtonSize.Small,
                                onClick = {
                                    editingId = identifier.id
                                    formTypeName = identifier.typeName
                                    formPlugin = identifier.plugin
                                    formOrder = identifier.order.toString()
                                    formColor = identifier.color
                                }
                            )
                            Button(
                                label = "Delete",
                                buttonType = ButtonType.GhostDanger,
                                buttonSize = ButtonSize.Small,
                                onClick = { requestDeleteId = identifier.id }
                            )
                        }
                    }
                }
            )
        }
    }
}