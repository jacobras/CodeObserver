package nl.jacobras.codebaseobserver.trends

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gabrieldrn.carbon.Carbon
import io.ktor.client.HttpClient
import nl.jacobras.codebaseobserver.ui.chart.TimeView
import nl.jacobras.codebaseobserver.ui.loading.ProgressIndicator

@Composable
internal fun Trends(
    client: HttpClient,
    projectId: String,
    timeView: TimeView,
    onSelectTimeView: (TimeView) -> Unit
) {
    val viewModel = viewModel { TrendsViewModel(client) }
    val metrics by viewModel.metrics.collectAsState(emptyList())
    val isLoading by viewModel.isLoading.collectAsState(false)
    val loadingError by viewModel.loadingError.collectAsState("")
    val updateError by viewModel.updateError.collectAsState("")

    LaunchedEffect(projectId) {
        viewModel.setProjectId(projectId)
    }

    if (isLoading || loadingError.isNotEmpty() || updateError.isNotEmpty()) {
        ProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            loading = isLoading,
            error = updateError.ifEmpty { loadingError },
            onRetry = if (loadingError.isNotEmpty()) {
                { viewModel.refresh() }
            } else {
                null
            }
        )
        return
    } else if (metrics.isEmpty()) {
        BasicText(
            modifier = Modifier.fillMaxWidth(),
            text = "No metrics found",
            style = Carbon.typography.body02
        )
        return
    }

    Column {
        CodeCharts(
            metrics = metrics,
            timeView = timeView,
            onSelectTimeView = onSelectTimeView
        )
        Spacer(Modifier.height(32.dp))
        CodeTable(
            metrics = metrics,
            onDelete = { viewModel.delete(it) }
        )
    }
}