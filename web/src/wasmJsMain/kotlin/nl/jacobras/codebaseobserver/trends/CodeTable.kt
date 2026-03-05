package nl.jacobras.codebaseobserver.trends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicText
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
                0 -> BasicText(
                    text = record.gitDate.toString(),
                    style = Carbon.typography.bodyCompact01,
                    modifier = modifier
                )
                1 -> BasicText(
                    text = record.gitHash.take(7),
                    style = Carbon.typography.code01,
                    modifier = modifier
                )
                2 -> BasicText(
                    text = HumanReadable.abbreviation(record.linesOfCode, decimals = 1),
                    style = Carbon.typography.bodyCompact01,
                    modifier = modifier
                )
                3 -> BasicText(
                    text = record.moduleCount.toString(),
                    style = Carbon.typography.bodyCompact01,
                    modifier = modifier
                )
                4 -> BasicText(
                    text = record.moduleTreeHeight.toString(),
                    style = Carbon.typography.bodyCompact01,
                    modifier = modifier
                )
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