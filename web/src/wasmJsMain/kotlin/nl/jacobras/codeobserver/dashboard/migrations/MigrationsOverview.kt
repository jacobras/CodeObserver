package nl.jacobras.codeobserver.dashboard.migrations

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.button.Button
import com.gabrieldrn.carbon.button.ButtonSize
import com.gabrieldrn.carbon.button.ButtonType
import nl.jacobras.codeobserver.auth.isCurrentUserAdmin
import nl.jacobras.codeobserver.dto.MigrationDto
import nl.jacobras.codeobserver.dto.MigrationId
import nl.jacobras.codeobserver.util.ui.dialog.DeleteDialog
import nl.jacobras.codeobserver.util.ui.table.DataTable

@Composable
internal fun MigrationsOverview(
    migrations: List<MigrationDto>,
    onSave: (id: MigrationId?, name: String, description: String, type: String, rule: String) -> Unit,
    onDelete: (id: MigrationId) -> Unit
) {
    var editingMigration by remember { mutableStateOf<MigrationDto?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var requestDeleteId by remember { mutableStateOf<MigrationId?>(null) }

    if (showForm) {
        MigrationFormDialog(
            migration = editingMigration,
            onSave = { name, description, type, rule ->
                onSave(editingMigration?.id, name, description, type, rule)
                showForm = false
                editingMigration = null
            },
            onCancel = {
                showForm = false
                editingMigration = null
            }
        )
    }

    requestDeleteId?.let {
        DeleteDialog(
            message = "Are you sure you want to delete this migration?",
            onCancel = { requestDeleteId = null },
            onDelete = {
                onDelete(it)
                requestDeleteId = null
            }
        )
    }

    Column(Modifier.fillMaxWidth()) {
        Button(
            label = "Add migration",
            buttonType = ButtonType.Primary,
            buttonSize = ButtonSize.Small,
            onClick = {
                editingMigration = null
                showForm = true
            }
        )
        Spacer(Modifier.height(16.dp))

        if (migrations.isEmpty()) {
            BasicText(
                text = "No migrations yet. Add one to get started.",
                style = Carbon.typography.body02
            )
        } else {
            val isAdmin = isCurrentUserAdmin()
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
                                    editingMigration = migration
                                    showForm = true
                                }
                            )
                            if (isAdmin) {
                                Button(
                                    label = "Delete",
                                    buttonType = ButtonType.GhostDanger,
                                    buttonSize = ButtonSize.Small,
                                    onClick = { requestDeleteId = migration.id }
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}