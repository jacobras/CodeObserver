package nl.jacobras.codeobserver.dashboard.modulegraph

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.jacobras.codeobserver.dto.GradleDto
import nl.jacobras.codeobserver.util.ui.chart.ChartColor
import nl.jacobras.codeobserver.util.ui.chart.TimeChart
import nl.jacobras.codeobserver.util.ui.chart.TimeView
import nl.jacobras.codeobserver.util.ui.chart.TimeViewSelector

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TimeChart(
                title = "Module count",
                records = gradle.metrics.filter { it.moduleCount > 0 },
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
                records = gradle.metrics.filter { it.moduleTreeHeight > 0 },
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
}