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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.Carbon
import io.ktor.client.HttpClient

@Composable
internal fun Trends(
    client: HttpClient,
    projectId: String
) {
    val viewModel = remember { TrendsViewModel(client) }
    val metrics by viewModel.metrics.collectAsState(emptyList())

    LaunchedEffect(projectId) {
        viewModel.setProjectId(projectId)
    }

    if (metrics.isEmpty()) {
        BasicText(
            modifier = Modifier.fillMaxWidth(),
            text = "No metrics found",
            style = Carbon.typography.body02
        )
        return
    }

    Column {
        CodeCharts(metrics)
        Spacer(Modifier.height(32.dp))
        CodeTable(
            metrics = metrics,
            onDelete = { viewModel.delete(it) }
        )
    }
}