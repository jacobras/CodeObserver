package nl.jacobras.codeobserver.dashboard.modulegraph

import androidx.compose.foundation.layout.Column
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
import com.gabrieldrn.carbon.Carbon
import nl.jacobras.codeobserver.dto.GradleDto
import nl.jacobras.codeobserver.util.ui.chart.ChartColor
import nl.jacobras.codeobserver.util.ui.chart.TimeChart
import nl.jacobras.codeobserver.util.ui.chart.TimeView
import nl.jacobras.codeobserver.util.ui.chart.TimeViewSelector
import nl.jacobras.codeobserver.util.ui.layout.DoubleChartWithDataTable
import nl.jacobras.codeobserver.util.ui.table.DataTable

@Composable
internal fun GradleMetrics(viewModel: ModuleGraphViewModel) {
    val gradle by viewModel.graphModules.collectAsState(GradleDto())
    var timeView by remember { mutableStateOf(TimeView.Last3Months) }

    Column {
        TimeViewSelector(
            selected = timeView,
            onSelect = { timeView = it }
        )
        Spacer(Modifier.height(16.dp))

        DoubleChartWithDataTable(
            modifier = Modifier.fillMaxWidth(),
            firstChart = { chartModifier ->
                TimeChart(
                    title = "Module count",
                    records = gradle.metrics.filter { it.moduleCount > 0 },
                    dateField = { it.gitDate },
                    metricField = { it.moduleCount },
                    timeView = timeView,
                    color = ChartColor.BurntSienna,
                    modifier = chartModifier
                )
            },
            secondChart = { chartModifier ->
                TimeChart(
                    title = "Module tree height",
                    records = gradle.metrics.filter { it.moduleTreeHeight > 0 },
                    dateField = { it.gitDate },
                    metricField = { it.moduleTreeHeight },
                    timeView = timeView,
                    color = ChartColor.Charcoal,
                    modifier = chartModifier
                )
            },
            dataTable = { tableModifier ->
                GradleMetricsTable(
                    gradle = gradle,
                    modifier = tableModifier
                )
            }
        )
    }
}

@Composable
private fun GradleMetricsTable(
    gradle: GradleDto,
    modifier: Modifier = Modifier
) {
    val points = gradle.metrics.sortedByDescending { it.gitDate }

    DataTable(
        modifier = modifier,
        columnHeadings = listOf("Git date", "Module count", "Module tree height"),
        rowCount = points.size,
        cellContent = { rowIndex, columnIndex, cellModifier ->
            val point = points[rowIndex]

            when (columnIndex) {
                0 -> SelectionContainer(cellModifier) {
                    BasicText(
                        text = point.gitDate.toString(),
                        style = Carbon.typography.bodyCompact01
                    )
                }
                1 -> SelectionContainer(cellModifier) {
                    BasicText(
                        text = point.moduleCount.toString(),
                        style = Carbon.typography.bodyCompact01
                    )
                }
                2 -> SelectionContainer(cellModifier) {
                    BasicText(
                        text = point.moduleTreeHeight.toString(),
                        style = Carbon.typography.bodyCompact01
                    )
                }
            }
        }
    )
}