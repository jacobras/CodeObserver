package nl.jacobras.codeobserver.dashboard.trends

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.jacobras.codeobserver.dto.CodeMetricsDto
import nl.jacobras.codeobserver.util.ui.chart.ChartColor
import nl.jacobras.codeobserver.util.ui.chart.TimeChart
import nl.jacobras.codeobserver.util.ui.chart.TimeView

@Composable
internal fun CodeCharts(
    metrics: List<CodeMetricsDto>,
    timeView: TimeView,
    modifier: Modifier = Modifier
) {
    TimeChart(
        title = "Lines of code",
        records = metrics.filter { it.linesOfCode > 0 },
        dateField = { it.gitDate },
        metricField = { it.linesOfCode },
        timeView = timeView,
        color = ChartColor.PersianGreen,
        modifier = modifier
    )
}