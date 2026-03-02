package nl.jacobras.codebaseobserver.ui.trends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.button.Button
import com.gabrieldrn.carbon.button.ButtonSize
import com.gabrieldrn.carbon.button.ButtonType
import nl.jacobras.codebaseobserver.dto.CodeMetricsDto
import nl.jacobras.humanreadable.HumanReadable

@Composable
internal fun CodeTable(
    metrics: List<CodeMetricsDto>,
    onDelete: (CodeMetricsDto) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            BasicText(
                text = "Git date",
                style = Carbon.typography.body02,
                modifier = Modifier.weight(1f)
            )
            BasicText(
                text = "Git hash",
                style = Carbon.typography.body02,
                modifier = Modifier.weight(1f)
            )
            BasicText(
                text = "Lines of code",
                style = Carbon.typography.body02,
                modifier = Modifier.weight(1f)
            )
            BasicText(
                text = "Module count",
                style = Carbon.typography.body02,
                modifier = Modifier.weight(1f)
            )
            BasicText(
                text = "Module tree height",
                style = Carbon.typography.body02,
                modifier = Modifier.weight(1f)
            )
            BasicText(
                text = "Actions",
                style = Carbon.typography.body02,
                modifier = Modifier.weight(1f)
            )
        }
        LazyColumn {
            items(metrics) { record ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicText(
                        text = record.gitDate.toString(),
                        style = Carbon.typography.body02,
                        modifier = Modifier.weight(1f)
                    )
                    BasicText(
                        text = record.gitHash.take(7),
                        style = Carbon.typography.body02,
                        modifier = Modifier.weight(1f)
                    )
                    BasicText(
                        text = HumanReadable.abbreviation(record.linesOfCode, decimals = 1),
                        style = Carbon.typography.body02,
                        modifier = Modifier.weight(1f)
                    )
                    BasicText(
                        text = record.moduleCount.toString(),
                        style = Carbon.typography.body02,
                        modifier = Modifier.weight(1f)
                    )
                    BasicText(
                        text = record.moduleTreeHeight.toString(),
                        style = Carbon.typography.body02,
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
        }
    }
}