@file:OptIn(ExperimentalWasmJsInterop::class)

package nl.jacobras.codebaseobserver.dashboard.detekt

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.button.Button
import com.gabrieldrn.carbon.button.ButtonSize
import com.gabrieldrn.carbon.button.ButtonType
import nl.jacobras.codebaseobserver.di.RepositoryLocator
import nl.jacobras.codebaseobserver.dto.DetektReportDto
import nl.jacobras.codebaseobserver.util.data.RequestState
import nl.jacobras.codebaseobserver.util.ui.UiState
import nl.jacobras.codebaseobserver.util.ui.chart.ChartColor
import nl.jacobras.codebaseobserver.util.ui.chart.TimeChart
import nl.jacobras.codebaseobserver.util.ui.chart.TimeView
import nl.jacobras.codebaseobserver.util.ui.chart.TimeViewSelector
import nl.jacobras.codebaseobserver.util.ui.loading.ProgressIndicator
import nl.jacobras.codebaseobserver.util.ui.table.DataTable
import nl.jacobras.codebaseobserver.util.ui.text.gitHashExcerpt

@Composable
internal fun DetektTrends(
    timeView: TimeView,
    onSelectTimeView: (TimeView) -> Unit
) {
    val viewModel = viewModel {
        DetektTrendsViewModel(
            detektReportRepository = RepositoryLocator.detektReportRepository,
            projectRepository = RepositoryLocator.projectRepository
        )
    }
    val reports by viewModel.reports.collectAsState(emptyList())
    val state by viewModel.state.collectAsState(UiState())

    Column {
        when (val loading = state.loading) {
            is RequestState.Working -> {
                ProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    loading = true
                )
            }
            is RequestState.Error -> {
                ProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    error = loading.type.name,
                    onRetry = { viewModel.refresh() }
                )
            }
            RequestState.Idle -> Unit
        }

        if (reports.isEmpty()) {
            BasicText(
                modifier = Modifier.fillMaxWidth(),
                text = "No Detekt reports found",
                style = Carbon.typography.body02
            )
            return
        }

        TimeViewSelector(
            selected = timeView,
            onSelect = onSelectTimeView
        )
        Spacer(Modifier.height(16.dp))

        DetektChartsAndTable(reports = reports, timeView = timeView)
    }
}

@Composable
private fun DetektChartsAndTable(
    reports: List<DetektReportDto>,
    timeView: TimeView
) {
    val reportsOldestFirst = reports.sortedBy { it.gitDate }
    val reportsNewestFirst = reportsOldestFirst.reversed()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TimeChart(
                modifier = Modifier.weight(1f),
                title = "Total findings",
                records = reportsOldestFirst,
                dateField = { it.gitDate },
                metricField = { it.findings },
                timeView = timeView,
                color = ChartColor.BurntSienna
            )
            TimeChart(
                modifier = Modifier.weight(1f),
                title = "Smells per 1,000 lloc",
                records = reportsOldestFirst,
                dateField = { it.gitDate },
                metricField = { it.smellsPer1000 },
                timeView = timeView,
                color = ChartColor.PersianGreen
            )
        }

        DataTable(
            modifier = Modifier.fillMaxWidth(),
            columnHeadings = listOf("Git date", "Git hash", "Findings", "Smells/1000 lloc", "Actions"),
            rowCount = reportsNewestFirst.size,
            cellContent = { rowIndex, columnIndex, modifier ->
                val item = reportsNewestFirst[rowIndex]
                when (columnIndex) {
                    0 -> SelectionContainer(modifier) {
                        BasicText(
                            text = item.gitDate.toString(),
                            style = Carbon.typography.bodyCompact01
                        )
                    }
                    1 -> SelectionContainer(modifier) {
                        BasicText(
                            text = item.gitHash.gitHashExcerpt(),
                            style = Carbon.typography.code01
                        )
                    }
                    2 -> SelectionContainer(modifier) {
                        BasicText(
                            text = item.findings.toString(),
                            style = Carbon.typography.bodyCompact01
                        )
                    }
                    3 -> SelectionContainer(modifier) {
                        BasicText(
                            text = item.smellsPer1000.toString(),
                            style = Carbon.typography.bodyCompact01
                        )
                    }
                    4 -> Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            label = "View",
                            onClick = { openHtmlInNewTab(item.htmlReport) },
                            buttonSize = ButtonSize.Small,
                            buttonType = ButtonType.Ghost
                        )
                        Button(
                            label = "Download",
                            onClick = { downloadHtmlFile(item.htmlReport) },
                            buttonSize = ButtonSize.Small,
                            buttonType = ButtonType.Ghost
                        )
                    }
                }
            }
        )
    }
}

@JsFun(
    """
    (html) => {
        const url = URL.createObjectURL(new Blob([html], {type: 'text/html'}));
        window.open(url, '_blank');
    }
"""
)
private external fun openHtmlInNewTab(html: String)

@JsFun(
    """
    (html) => {
        const a = document.createElement('a');
        const url = URL.createObjectURL(new Blob([html], {type: 'text/html'}));
        a.href = url;
        a.download = 'detekt-report.html';
        a.click();
        URL.revokeObjectURL(url);
    }"""
)
private external fun downloadHtmlFile(html: String)