package nl.jacobras.codebaseobserver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DividerProperties
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import nl.jacobras.codebaseobserver.ui.chart.buildChartData

@Composable
internal fun DashboardScreen(
    records: List<CountRecord>,
    error: String?,
    gitHashInput: String,
    gitDateInput: String,
    fileCountInput: String,
    isEditing: Boolean,
    onGitHashChange: (String) -> Unit,
    onGitDateChange: (String) -> Unit,
    onFileCountChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onClear: () -> Unit,
    onEdit: (CountRecord) -> Unit,
    onDelete: (CountRecord) -> Unit
) {
    Text("Dashboard", style = MaterialTheme.typography.headlineLarge)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Add or update count", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = gitHashInput,
            onValueChange = onGitHashChange,
            label = { Text("Git hash") },
            enabled = !isEditing,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = gitDateInput,
            onValueChange = onGitDateChange,
            label = { Text("Git date (ISO)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = fileCountInput,
            onValueChange = onFileCountChange,
            label = { Text("File count") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onSubmit) {
                Text(if (isEditing) "Update" else "Add")
            }
            Button(
                onClick = onClear,
                enabled = gitHashInput.isNotEmpty() || gitDateInput.isNotEmpty() || fileCountInput.isNotEmpty()
            ) {
                Text("Clear")
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
        Chart(records, timeView)
        RecordsTable(
            records = records,
            onEdit = onEdit,
            onDelete = onDelete
        )
    }
}

@Composable
private fun Chart(records: List<CountRecord>, timeView: TimeView) {
    val chartData = buildChartData(records, timeView)
    val lineData = remember(records, timeView) {
        listOf(
            Line(
                label = "File count",
                color = SolidColor(Color(0xFF264653)),
                values = chartData.yValues.map { it.toDouble() },
                firstGradientFillColor = Color(0xFF2A9D8F).copy(alpha = 0.35f),
                secondGradientFillColor = Color.Transparent,
                dotProperties = DotProperties(
                    enabled = true,
                    color = SolidColor(Color.White),
                    strokeColor = SolidColor(Color(0xFF2A9D8F)),
                    radius = 6.dp,
                    strokeWidth = 3.dp
                )
            )
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        LineChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(horizontal = 12.dp),
            data = lineData,
            curvedEdges = false,
            animationMode = AnimationMode.None,
            gridProperties = GridProperties(
                enabled = true,
                xAxisProperties = GridProperties.AxisProperties(enabled = false),
                yAxisProperties = GridProperties.AxisProperties(
                    enabled = true,
                    thickness = 1.dp,
                    color = SolidColor(Color(0xFFE0E0E0))
                )
            ),
            indicatorProperties = HorizontalIndicatorProperties(
                enabled = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF2F4858)),
                padding = 12.dp
            ),
            labelProperties = LabelProperties(
                enabled = true,
                labels = chartData.xLabels,
                textStyle = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF2F4858)),
                rotation = LabelProperties.Rotation(degree = 0f)
            ),
            dividerProperties = DividerProperties(enabled = false)
        )
    }
}

@Composable
private fun RecordsTable(
    records: List<CountRecord>,
    onEdit: (CountRecord) -> Unit,
    onDelete: (CountRecord) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Git Date", modifier = Modifier.weight(1f))
            Text("Files", modifier = Modifier.weight(1f))
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
                    Text(record.fileCount.toString(), modifier = Modifier.weight(1f))
                    Text(record.gitHash.take(7), modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(onClick = { onEdit(record) }) {
                            Text("Edit", color = Color(0xFF2A9D8F))
                        }
                        TextButton(onClick = { onDelete(record) }) {
                            Text("Delete", color = Color(0xFFE76F51))
                        }
                    }
                }
            }
        }
    }
}

internal enum class TimeView(val label: String) {
    Last7Days("Last 7 days"),
    Last30Days("Last 30 days"),
    Last6Months("Last 6 months")
}