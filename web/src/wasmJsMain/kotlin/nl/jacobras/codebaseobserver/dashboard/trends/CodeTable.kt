package nl.jacobras.codebaseobserver.dashboard.trends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.button.Button
import com.gabrieldrn.carbon.button.ButtonSize
import com.gabrieldrn.carbon.button.ButtonType
import nl.jacobras.codebaseobserver.dto.CodeMetricsDto
import nl.jacobras.codebaseobserver.util.ui.dialog.DeleteDialog
import nl.jacobras.codebaseobserver.util.ui.table.DataTable
import nl.jacobras.codebaseobserver.util.ui.text.gitHashExcerpt
import nl.jacobras.humanreadable.HumanReadable

@Composable
internal fun CodeTable(
    metrics: List<CodeMetricsDto>,
    onDelete: (CodeMetricsDto) -> Unit
) {
    val sortedMetrics = metrics.sortedByDescending { it.gitDate }
    var requestDeleteRecord by remember { mutableStateOf<CodeMetricsDto?>(null) }

    if (requestDeleteRecord != null) {
        DeleteDialog(
            message = "Are you sure you want to delete this record?",
            onCancel = { requestDeleteRecord = null },
            onDelete = {
                onDelete(requestDeleteRecord!!)
                requestDeleteRecord = null
            }
        )
    }

    DataTable(
        columnHeadings = listOf(
            "Git date",
            "Git hash",
            "Lines of code",
            "Module count",
            "Module tree height",
            "Actions"
        ),
        rowCount = sortedMetrics.size,
        cellContent = { rowIndex, columnIndex, modifier ->
            val record = sortedMetrics[rowIndex]

            when (columnIndex) {
                0 -> SelectionContainer(modifier) {
                    BasicText(
                        text = record.gitDate.toString(),
                        style = Carbon.typography.bodyCompact01
                    )
                }
                1 -> SelectionContainer(modifier) {
                    BasicText(
                        text = record.gitHash.gitHashExcerpt(),
                        style = Carbon.typography.code01
                    )
                }
                2 -> SelectionContainer(modifier) {
                    BasicText(
                        text = HumanReadable.abbreviation(record.linesOfCode, decimals = 1),
                        style = Carbon.typography.bodyCompact01
                    )
                }
                3 -> SelectionContainer(modifier) {
                    BasicText(
                        text = record.moduleCount.toString(),
                        style = Carbon.typography.bodyCompact01
                    )
                }
                4 -> SelectionContainer(modifier) {
                    BasicText(
                        text = record.moduleTreeHeight.toString(),
                        style = Carbon.typography.bodyCompact01
                    )
                }
                5 -> Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = modifier
                ) {
                    Button(
                        label = "Delete",
                        buttonType = ButtonType.GhostDanger,
                        buttonSize = ButtonSize.Small,
                        onClick = { requestDeleteRecord = record }
                    )
                }
            }
        }
    )
}