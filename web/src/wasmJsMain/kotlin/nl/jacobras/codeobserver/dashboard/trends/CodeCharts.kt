package nl.jacobras.codeobserver.dashboard.trends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.jacobras.codeobserver.dto.CodeMetricsDto
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.codeobserver.util.ui.chart.ChartColor
import nl.jacobras.codeobserver.util.ui.chart.TimeChart
import nl.jacobras.codeobserver.util.ui.chart.TimeView
import nl.jacobras.codeobserver.util.ui.chart.TimeViewSelector
import nl.jacobras.codeobserver.util.ui.commandinfo.CommandInfoBox

@Composable
internal fun CodeCharts(
    metrics: List<CodeMetricsDto>,
    timeView: TimeView,
    onSelectTimeView: (TimeView) -> Unit,
    projectId: ProjectId?
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TimeViewSelector(
            selected = timeView,
            onSelect = { onSelectTimeView(it) }
        )
        if (projectId != null) {
            Spacer(Modifier.weight(1f))
            CommandInfoBox(
                command = "measure",
                projectId = projectId
            )
        }
    }
    Spacer(Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TimeChart(
            title = "Lines of code",
            records = metrics.filter { it.linesOfCode > 0 },
            dateField = { it.gitDate },
            metricField = { it.linesOfCode },
            timeView = timeView,
            color = ChartColor.PersianGreen,
            modifier = Modifier
                .weight(1f)
                .height(240.dp)
        )
        TimeChart(
            title = "Module count",
            records = metrics.filter { it.moduleCount > 0 },
            dateField = { it.gitDate },
            metricField = { it.moduleCount },
            timeView = timeView,
            color = ChartColor.BurntSienna,
            modifier = Modifier
                .weight(1f)
                .height(240.dp)
        )
        TimeChart(
            title = "Module tree height",
            records = metrics.filter { it.moduleTreeHeight > 0 },
            dateField = { it.gitDate },
            metricField = { it.moduleTreeHeight },
            timeView = timeView,
            color = ChartColor.Charcoal,
            modifier = Modifier
                .weight(1f)
                .height(240.dp)
        )
    }
}