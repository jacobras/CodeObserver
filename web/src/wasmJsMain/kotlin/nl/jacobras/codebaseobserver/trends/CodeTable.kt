package nl.jacobras.codebaseobserver.trends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.button.Button
import com.gabrieldrn.carbon.button.ButtonSize
import com.gabrieldrn.carbon.button.ButtonType
import nl.jacobras.codebaseobserver.dto.CodeMetricsDto
import nl.jacobras.codebaseobserver.ui.table.DataTable
import nl.jacobras.humanreadable.HumanReadable

@Composable
internal fun CodeTable(
    metrics: List<CodeMetricsDto>,
    onDelete: (CodeMetricsDto) -> Unit
) {
    DataTable(
        columnHeadings = listOf(
            "Git date",
            "Git hash",
            "Lines of code",
            "Module count",
            "Module tree height",
            "Actions"
        ),
        rowCount = metrics.size,
        cellContent = { rowIndex, columnIndex, modifier ->
            val record = metrics[rowIndex]

            when (columnIndex) {
                0 -> SelectionContainer(modifier) {
                    BasicText(
                        text = record.gitDate.toString(),
                        style = Carbon.typography.bodyCompact01
                    )
                }
                1 -> SelectionContainer(modifier) {
                    BasicText(
                        text = record.gitHash.take(7),
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
                        onClick = { onDelete(record) }
                    )
                }
            }
        }
    )
}