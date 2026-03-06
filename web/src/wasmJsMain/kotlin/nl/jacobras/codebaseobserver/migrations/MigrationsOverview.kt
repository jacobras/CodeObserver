package nl.jacobras.codebaseobserver.migrations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.button.Button
import com.gabrieldrn.carbon.button.ButtonSize
import com.gabrieldrn.carbon.button.ButtonType
import com.gabrieldrn.carbon.dropdown.Dropdown
import com.gabrieldrn.carbon.dropdown.base.DropdownInteractiveState
import com.gabrieldrn.carbon.dropdown.base.DropdownOption
import com.gabrieldrn.carbon.textinput.TextInput
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.launch
import nl.jacobras.codebaseobserver.dto.MigrationDto
import nl.jacobras.codebaseobserver.dto.MigrationRequest
import nl.jacobras.codebaseobserver.dto.MigrationUpdateRequest
import nl.jacobras.codebaseobserver.ui.table.DataTable

@Composable
fun MigrationsOverview(
    client: HttpClient,
    projectId: String,
    migrations: List<MigrationDto>,
    onRefresh: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var editingId by remember { mutableStateOf<Int?>(null) }
    var formName by remember { mutableStateOf("") }
    var formDescription by remember { mutableStateOf("") }
    var formType by remember { mutableStateOf("moduleUsage") }
    var formRule by remember { mutableStateOf("") }

    val isEditing = editingId != null

    fun clearForm() {
        editingId = null
        formName = ""
        formDescription = ""
        formType = "moduleUsage"
        formRule = ""
    }

    Column(Modifier.fillMaxWidth()) {
        TextInput(
            label = "Name",
            value = formName,
            onValueChange = { formName = it },
            placeholderText = "Remove deprecated module"
        )
        Spacer(Modifier.height(8.dp))
        TextInput(
            label = "Description",
            value = formDescription,
            onValueChange = { formDescription = it },
            placeholderText = "Optional description"
        )
        if (!isEditing) {
            Spacer(Modifier.height(8.dp))
            Dropdown(
                label = "Type",
                placeholder = "Select type",
                options = migrationTypes,
                selectedOption = formType,
                onOptionSelected = { formType = it },
                state = DropdownInteractiveState.Enabled
            )
            Spacer(Modifier.height(8.dp))
            TextInput(
                label = "Rule",
                value = formRule,
                onValueChange = { formRule = it },
                placeholderText = if (formType == "moduleUsage") {
                    "util:deprecated"
                } else {
                    "com.example.lib.Foo or com.example.*"
                }
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                label = if (isEditing) "Update" else "Add migration",
                buttonType = ButtonType.Primary,
                buttonSize = ButtonSize.Small,
                isEnabled = formName.trim().isNotEmpty() && (isEditing || formRule.trim().isNotEmpty()),
                onClick = {
                    val id = editingId
                    scope.launch {
                        if (id != null) {
                            client.patch("/migrations/$id") {
                                contentType(ContentType.Application.Json)
                                setBody(
                                    MigrationUpdateRequest(
                                        name = formName.trim(),
                                        description = formDescription.trim()
                                    )
                                )
                            }
                        } else {
                            client.post("/migrations") {
                                contentType(ContentType.Application.Json)
                                setBody(
                                    MigrationRequest(
                                        projectId = projectId,
                                        name = formName.trim(),
                                        description = formDescription.trim(),
                                        type = formType,
                                        rule = formRule.trim()
                                    )
                                )
                            }
                        }
                        clearForm()
                        onRefresh()
                    }
                }
            )
            Button(
                label = "Clear",
                buttonType = ButtonType.Tertiary,
                buttonSize = ButtonSize.Small,
                isEnabled = formName.isNotEmpty() || formDescription.isNotEmpty() || formRule.isNotEmpty() || isEditing,
                onClick = { clearForm() }
            )
        }
        Spacer(Modifier.height(20.dp))

        if (migrations.isEmpty()) {
            BasicText(
                text = "No migrations yet. Add one above.",
                style = Carbon.typography.body02
            )
        } else {
            DataTable(
                columnHeadings = listOf("Name", "Type", "Rule", "Actions"),
                rowCount = migrations.size,
                cellContent = { rowIndex, columnIndex, modifier ->
                    val migration = migrations[rowIndex]
                    when (columnIndex) {
                        0 -> SelectionContainer(modifier) {
                            BasicText(
                                text = migration.name,
                                style = Carbon.typography.bodyCompact01
                            )
                        }
                        1 -> SelectionContainer(modifier) {
                            BasicText(
                                text = migration.type,
                                style = Carbon.typography.bodyCompact01
                            )
                        }
                        2 -> SelectionContainer(modifier) {
                            BasicText(
                                text = migration.rule,
                                style = Carbon.typography.code01
                            )
                        }
                        3 -> Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = modifier
                        ) {
                            Button(
                                label = "Edit",
                                buttonType = ButtonType.Ghost,
                                buttonSize = ButtonSize.Small,
                                onClick = {
                                    editingId = migration.id
                                    formName = migration.name
                                    formDescription = migration.description
                                    formType = migration.type
                                    formRule = migration.rule
                                }
                            )
                            Button(
                                label = "Delete",
                                buttonType = ButtonType.GhostDanger,
                                buttonSize = ButtonSize.Small,
                                onClick = {
                                    scope.launch {
                                        client.delete("/migrations/${migration.id}")
                                        onRefresh()
                                    }
                                }
                            )
                        }
                    }
                }
            )
        }
    }
}

private val migrationTypes = linkedMapOf(
    "moduleUsage" to DropdownOption("moduleUsage"),
    "importUsage" to DropdownOption("importUsage")
)