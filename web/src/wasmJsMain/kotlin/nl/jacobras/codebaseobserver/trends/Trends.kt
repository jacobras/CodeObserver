package nl.jacobras.codebaseobserver.trends

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.Carbon
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import kotlinx.coroutines.launch
import nl.jacobras.codebaseobserver.dto.CodeMetricsDto

@Composable
internal fun Trends(
    client: HttpClient,
    projectId: String
) {
    val metrics by produceState(emptyList<CodeMetricsDto>(), projectId) {
        value = client.get("/metrics") {
            url { parameters.append("projectId", projectId) }
        }.body()
    }

    if (metrics.isEmpty()) {
        BasicText(
            modifier = Modifier.fillMaxWidth(),
            text = "No metrics found",
            style = Carbon.typography.body02
        )
        return
    }

    val scope = rememberCoroutineScope()
    Column {
        CodeCharts(metrics)
        Spacer(Modifier.height(32.dp))
        CodeTable(
            metrics = metrics,
            onDelete = { record ->
                scope.launch {
                    client.delete("/metrics/${record.gitHash}") {
                        url { parameters.append("projectId", projectId) }
                    }
                }
            }
        )
    }
}