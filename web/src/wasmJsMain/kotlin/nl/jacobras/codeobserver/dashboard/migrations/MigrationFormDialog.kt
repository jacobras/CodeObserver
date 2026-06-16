package nl.jacobras.codeobserver.dashboard.migrations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
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
import com.gabrieldrn.carbon.dropdown.Dropdown
import com.gabrieldrn.carbon.dropdown.base.DropdownInteractiveState
import com.gabrieldrn.carbon.dropdown.base.DropdownOption
import com.gabrieldrn.carbon.foundation.color.CarbonLayer
import com.gabrieldrn.carbon.foundation.color.layerBackground
import com.gabrieldrn.carbon.textinput.TextInput
import nl.jacobras.codeobserver.dto.MigrationDto

@Composable
internal fun MigrationFormDialog(
    migration: MigrationDto?,
    onSave: (name: String, description: String, type: String, rule: String) -> Unit,
    onCancel: () -> Unit
) {
    val isEditing = migration != null
    var formName by remember { mutableStateOf(migration?.name ?: "") }
    var formDescription by remember { mutableStateOf(migration?.description ?: "") }
    var formType by remember { mutableStateOf(migration?.type ?: "moduleUsage") }
    var formRule by remember { mutableStateOf(migration?.rule ?: "") }

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
                        text = if (isEditing) "Edit migration" else "Add migration",
                        style = Carbon.typography.heading03.copy(color = Carbon.theme.textPrimary)
                    )
                    Spacer(Modifier.height(16.dp))
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
                        Button(
                            label = if (isEditing) "Update" else "Add migration",
                            buttonType = ButtonType.Primary,
                            buttonSize = ButtonSize.Small,
                            isEnabled = formName.trim().isNotEmpty() && (isEditing || formRule.trim().isNotEmpty()),
                            onClick = {
                                onSave(formName.trim(), formDescription.trim(), formType, formRule.trim())
                            }
                        )
                    }
                }
            }
        }
    )
}

private val migrationTypes = linkedMapOf(
    "moduleUsage" to DropdownOption("moduleUsage"),
    "importUsage" to DropdownOption("importUsage")
)