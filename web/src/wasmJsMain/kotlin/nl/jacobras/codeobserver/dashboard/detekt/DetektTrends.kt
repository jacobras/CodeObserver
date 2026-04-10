@file:OptIn(ExperimentalWasmJsInterop::class)

package nl.jacobras.codeobserver.dashboard.detekt

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.button.ButtonType
import nl.jacobras.codeobserver.di.RepositoryLocator
import nl.jacobras.codeobserver.dto.DetektMetricDto
import nl.jacobras.codeobserver.dto.ReportId
import nl.jacobras.codeobserver.util.data.RequestState
import nl.jacobras.codeobserver.util.ui.UiState
import nl.jacobras.codeobserver.util.ui.button.SmallProgressButton
import nl.jacobras.codeobserver.util.ui.chart.ChartColor
import nl.jacobras.codeobserver.util.ui.chart.TimeChart
import nl.jacobras.codeobserver.util.ui.chart.TimeView
import nl.jacobras.codeobserver.util.ui.chart.TimeViewSelector
import nl.jacobras.codeobserver.util.ui.commandinfo.CommandInfoBox
import nl.jacobras.codeobserver.util.ui.dialog.DeleteDialog
import nl.jacobras.codeobserver.util.ui.progress.EmptyState
import nl.jacobras.codeobserver.util.ui.progress.ProgressIndicator
import nl.jacobras.codeobserver.util.ui.table.DataTable
import nl.jacobras.codeobserver.util.ui.text.excerpt

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
    val reports by viewModel.metrics.collectAsState(emptyList())
    val state by viewModel.metricsState.collectAsState(UiState())
    val projectId by viewModel.projectId.collectAsState()

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
            EmptyState(
                text = "No Detekt reports found",
                command = "report-detekt --htmlFile=build/reports/detekt/detekt.html",
                projectId = projectId ?: return
            )
            return
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            TimeViewSelector(
                selected = timeView,
                onSelect = onSelectTimeView
            )
            projectId?.let {
                Spacer(Modifier.weight(1f))
                CommandInfoBox(
                    command = "report-detekt --htmlFile=build/reports/detekt/detekt.html",
                    projectId = it
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        DetektChartsAndTable(
            reports = reports,
            deleting = state.deleting,
            timeView = timeView,
            onDelete = { viewModel.delete(it) }
        )
    }
}

@Composable
private fun DetektChartsAndTable(
    reports: List<DetektMetricDto>,
    deleting: Map<ReportId, RequestState>,
    timeView: TimeView,
    onDelete: (DetektMetricDto) -> Unit
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

        var requestDeleteRecord by remember { mutableStateOf<DetektMetricDto?>(null) }
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
            modifier = Modifier.fillMaxWidth(),
            columnHeadings = listOf("Git date", "Git hash", "Findings", "Smells/1000 lloc", "Actions"),
            rowCount = reportsNewestFirst.size,
            cellContent = { rowIndex, columnIndex, modifier ->
                val record = reportsNewestFirst[rowIndex]
                when (columnIndex) {
                    0 -> SelectionContainer(modifier) {
                        BasicText(
                            text = record.gitDate.toString(),
                            style = Carbon.typography.bodyCompact01
                        )
                    }
                    1 -> SelectionContainer(modifier) {
                        BasicText(
                            text = record.gitHash.excerpt(),
                            style = Carbon.typography.code01
                        )
                    }
                    2 -> SelectionContainer(modifier) {
                        BasicText(
                            text = record.findings.toString(),
                            style = Carbon.typography.bodyCompact01
                        )
                    }
                    3 -> SelectionContainer(modifier) {
                        BasicText(
                            text = record.smellsPer1000.toString(),
                            style = Carbon.typography.bodyCompact01
                        )
                    }
                    4 -> Row(
                        modifier = modifier,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isDeleting = deleting[record.id] is RequestState.Working
                        SmallProgressButton(
                            label = "Delete",
                            buttonType = ButtonType.GhostDanger,
                            loading = isDeleting,
                            onClick = { requestDeleteRecord = record }
                        )
                    }
                }
            }
        )
    }
}