package nl.jacobras.codebaseobserver.ui.trends

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.jacobras.codebaseobserver.dto.CodeMetricsDto

@Composable
internal fun Trends(
    metrics: List<CodeMetricsDto>,
    onDelete: (CodeMetricsDto) -> Unit
) {
    Column {
        CodeCharts(metrics)
        Spacer(Modifier.height(32.dp))
        CodeTable(metrics, onDelete)
    }
}