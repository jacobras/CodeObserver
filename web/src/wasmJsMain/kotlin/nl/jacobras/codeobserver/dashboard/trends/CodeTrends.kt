package nl.jacobras.codeobserver.dashboard.trends

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.jacobras.codeobserver.util.data.RequestState
import nl.jacobras.codeobserver.util.ui.UiState
import nl.jacobras.codeobserver.util.ui.chart.TimeView
import nl.jacobras.codeobserver.util.ui.chart.TimeViewSelector
import nl.jacobras.codeobserver.util.ui.commandinfo.CommandInfoBox
import nl.jacobras.codeobserver.util.ui.layout.SingleChartWithDataTable
import nl.jacobras.codeobserver.util.ui.progress.EmptyState
import nl.jacobras.codeobserver.util.ui.progress.ProgressIndicator
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun CodeTrends(
    timeView: TimeView,
    onSelectTimeView: (TimeView) -> Unit
) {
    val viewModel = koinViewModel<TrendsViewModel>()
    val projectId by viewModel.projectId.collectAsState()
    val metrics by viewModel.metrics.collectAsState(emptyList())
    val state by viewModel.uiState.collectAsState(UiState())

    when (val loading = state.loading) {
        is RequestState.Working -> {
            ProgressIndicator(modifier = Modifier.fillMaxWidth(), loading = true)
            return
        }
        is RequestState.Error -> {
            ProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                error = loading.type.name,
                onRetry = { viewModel.refresh() }
            )
            return
        }
        RequestState.Idle -> Unit
    }

    val deletingError = state.deleting.values.filterIsInstance<RequestState.Error>().firstOrNull()
    if (deletingError != null) {
        ProgressIndicator(modifier = Modifier.fillMaxWidth(), error = deletingError.type.name)
        return
    }

    if (metrics.isEmpty()) {
        EmptyState(
            text = "No metrics found",
            command = "measure",
            projectId = projectId ?: return
        )
        return
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TimeViewSelector(
                selected = timeView,
                onSelect = { onSelectTimeView(it) }
            )
            val currentProjectId = projectId
            if (currentProjectId != null) {
                Spacer(Modifier.weight(1f))
                CommandInfoBox(
                    command = "measure",
                    projectId = currentProjectId
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        SingleChartWithDataTable(
            modifier = Modifier.fillMaxWidth(),
            chart = { chartModifier ->
                CodeCharts(
                    metrics = metrics,
                    timeView = timeView,
                    modifier = chartModifier
                )
            },
            dataTable = { tableModifier ->
                CodeTable(
                    metrics = metrics,
                    onDelete = { viewModel.delete(it) },
                    modifier = tableModifier
                )
            }
        )
    }
}