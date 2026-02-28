package nl.jacobras.codebaseobserver.ui.trends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.contentswitcher.ContentSwitcher
import nl.jacobras.codebaseobserver.dto.CodeMetricsDto
import nl.jacobras.codebaseobserver.ui.chart.TimeChart
import nl.jacobras.codebaseobserver.ui.chart.TimeView

@Composable
internal fun CodeCharts(
    metrics: List<CodeMetricsDto>
) {
    var timeView by remember { mutableStateOf(TimeView.Last7Days) }

    ContentSwitcher(
        options = TimeView.entries.map { it.label },
        selectedOption = timeView.label,
        onOptionSelected = { selected ->
            timeView = TimeView.entries.first { it.label == selected }
        }
    )
    Spacer(Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TimeChart(
            title = "Lines of code",
            records = metrics,
            dateField = { it.gitDate },
            metricField = { it.linesOfCode },
            timeView = timeView,
            color = Color(0xFF2A9D8F),
            modifier = Modifier
                .weight(1f)
                .height(240.dp)
        )
        TimeChart(
            title = "Module count",
            records = metrics,
            dateField = { it.gitDate },
            metricField = { it.moduleCount },
            timeView = timeView,
            color = Color(0xFFE76F51),
            modifier = Modifier
                .weight(1f)
                .height(240.dp)
        )
        TimeChart(
            title = "Module tree height",
            records = metrics,
            dateField = { it.gitDate },
            metricField = { it.moduleTreeHeight },
            timeView = timeView,
            color = Color(0xFF264653),
            modifier = Modifier
                .weight(1f)
                .height(240.dp)
        )
    }
}