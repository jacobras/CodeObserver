package nl.jacobras.codebaseobserver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import nl.jacobras.codebaseobserver.dto.MetricsDto
import nl.jacobras.codebaseobserver.ui.chart.GradleChart
import nl.jacobras.codebaseobserver.ui.chart.LinesOfCodeChart
import nl.jacobras.codebaseobserver.ui.chart.ModuleTreeHeightChart
import nl.jacobras.codebaseobserver.ui.chart.TimeView

@Composable
internal fun DashboardScreen(
    records: List<MetricsDto>,
    error: String?,
    projectIds: List<String>,
    selectedProjectId: String,
    onProjectIdChange: (String) -> Unit,
    onDelete: (MetricsDto) -> Unit
) {
    Text("Dashboard", style = MaterialTheme.typography.headlineLarge)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Project", style = MaterialTheme.typography.titleMedium)
        if (projectIds.isEmpty()) {
            Text("No projects yet. Create one by submitting data via the CLI.")
        } else {
            projectIds.forEach { projectId ->
                val selected = projectId == selectedProjectId
                if (selected) {
                    Button(onClick = { onProjectIdChange(projectId) }) {
                        Text(projectId)
                    }
                } else {
                    TextButton(onClick = { onProjectIdChange(projectId) }) {
                        Text(projectId)
                    }
                }
            }
        }
    }
    if (error != null) {
        Text("Error: $error")
    }
    if (records.isEmpty()) {
        Text("No data yet. Submit counts via the CLI.")
    } else {
        var timeView by remember { mutableStateOf(TimeView.Last7Days) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("View")
            TimeView.entries.forEach { view ->
                val selected = view == timeView
                if (selected) {
                    Button(onClick = { timeView = view }) {
                        Text(view.label)
                    }
                } else {
                    TextButton(onClick = { timeView = view }) {
                        Text(view.label)
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                LinesOfCodeChart(records, timeView)
            }
            Column(modifier = Modifier.weight(1f)) {
                GradleChart(records, timeView)
            }
            Column(modifier = Modifier.weight(1f)) {
                ModuleTreeHeightChart(records, timeView)
            }
        }
        RecordsTable(
            records = records,
            onDelete = onDelete
        )
    }
}

@Composable
private fun RecordsTable(
    records: List<MetricsDto>,
    onDelete: (MetricsDto) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Git Date", modifier = Modifier.weight(1f))
            Text("Lines of code", modifier = Modifier.weight(1f))
            Text("Hash", modifier = Modifier.weight(1f))
            Text("Actions", modifier = Modifier.weight(1f))
        }
        LazyColumn {
            items(records) { record ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(record.gitDate.toString(), modifier = Modifier.weight(1f))
                    Text(record.linesOfCode.toString(), modifier = Modifier.weight(1f))
                    Text(record.gitHash.take(7), modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(onClick = { onDelete(record) }) {
                            Text("Delete", color = Color(0xFFE76F51))
                        }
                    }
                }
            }
        }
    }
}