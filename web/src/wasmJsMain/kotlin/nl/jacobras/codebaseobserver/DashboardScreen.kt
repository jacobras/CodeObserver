package nl.jacobras.codebaseobserver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.button.Button
import com.gabrieldrn.carbon.button.ButtonSize
import com.gabrieldrn.carbon.button.ButtonType
import com.gabrieldrn.carbon.contentswitcher.ContentSwitcher
import com.gabrieldrn.carbon.foundation.color.CarbonLayer
import com.gabrieldrn.carbon.foundation.color.layerBackground
import com.gabrieldrn.carbon.tab.TabItem
import com.gabrieldrn.carbon.tab.TabList
import com.gabrieldrn.carbon.tab.TabVariant
import nl.jacobras.codebaseobserver.dto.MetricsDto
import nl.jacobras.codebaseobserver.ui.chart.Chart
import nl.jacobras.codebaseobserver.ui.chart.TimeView
import nl.jacobras.humanreadable.HumanReadable

@Composable
internal fun DashboardScreen(
    records: List<MetricsDto>,
    error: String?,
    projectIds: List<String>,
    selectedProjectId: String,
    onSelectProject: (String) -> Unit,
    onDelete: (MetricsDto) -> Unit
) {
    Column {
        BasicText(
            text = "Dashboard",
            style = Carbon.typography.heading06
        )
        Spacer(Modifier.height(16.dp))
        if (projectIds.isEmpty()) {
            BasicText(
                text = "No projects yet. Create one by submitting data via the CLI.",
                style = Carbon.typography.body02
            )
            Spacer(Modifier.height(16.dp))
        } else {
            val tabs = projectIds.map { TabItem(label = it) }
            TabList(
                tabs = tabs,
                variant = TabVariant.Contained,
                selectedTab = tabs.first { it.label == selectedProjectId },
                onTabSelected = { tab ->
                    onSelectProject(tab.label)
                }
            )
        }

        CarbonLayer {
            Column(
                modifier = Modifier
                    .layerBackground()
                    .padding(16.dp)
            ) {
                if (error != null) {
                    BasicText(
                        text = "Error: $error",
                        style = Carbon.typography.body02.copy(color = Carbon.theme.supportError)
                    )
                }
                if (records.isEmpty()) {
                    BasicText(
                        text = "No data yet. Submit counts via the CLI.",
                        style = Carbon.typography.body02
                    )
                } else {
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
                        Column(modifier = Modifier.weight(1f)) {
                            Chart(
                                title = "Lines of code",
                                records = records,
                                dateField = { it.gitDate },
                                metricField = { it.linesOfCode },
                                timeView = timeView,
                                color = Color(0xFF2A9D8F)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Chart(
                                title = "Module count",
                                records = records,
                                dateField = { it.gitDate },
                                metricField = { it.moduleCount },
                                timeView = timeView,
                                color = Color(0xFFE76F51)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Chart(
                                title = "Module tree height",
                                records = records,
                                dateField = { it.gitDate },
                                metricField = { it.moduleTreeHeight },
                                timeView = timeView,
                                color = Color(0xFF264653)
                            )
                        }
                    }
                    Spacer(Modifier.height(32.dp))

                    RecordsTable(
                        records = records,
                        onDelete = onDelete
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordsTable(
    records: List<MetricsDto>,
    onDelete: (MetricsDto) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            BasicText(
                text = "Git date",
                style = Carbon.typography.body02,
                modifier = Modifier.weight(1f)
            )
            BasicText(
                text = "Git hash",
                style = Carbon.typography.body02,
                modifier = Modifier.weight(1f)
            )
            BasicText(
                text = "Lines of code",
                style = Carbon.typography.body02,
                modifier = Modifier.weight(1f)
            )
            BasicText(
                text = "Module count",
                style = Carbon.typography.body02,
                modifier = Modifier.weight(1f)
            )
            BasicText(
                text = "Module tree height",
                style = Carbon.typography.body02,
                modifier = Modifier.weight(1f)
            )
            BasicText(
                text = "Actions",
                style = Carbon.typography.body02,
                modifier = Modifier.weight(1f)
            )
        }
        LazyColumn {
            items(records) { record ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicText(
                        text = record.gitDate.toString(),
                        style = Carbon.typography.body02,
                        modifier = Modifier.weight(1f)
                    )
                    BasicText(
                        text = record.gitHash.take(7),
                        style = Carbon.typography.body02,
                        modifier = Modifier.weight(1f)
                    )
                    BasicText(
                        text = HumanReadable.abbreviation(record.linesOfCode, decimals = 1),
                        style = Carbon.typography.body02,
                        modifier = Modifier.weight(1f)
                    )
                    BasicText(
                        text = record.moduleCount.toString(),
                        style = Carbon.typography.body02,
                        modifier = Modifier.weight(1f)
                    )
                    BasicText(
                        text = record.moduleTreeHeight.toString(),
                        style = Carbon.typography.body02,
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            label = "Delete",
                            buttonType = ButtonType.GhostDanger,
                            buttonSize = ButtonSize.Small,
                            onClick = { onDelete(record) }
                        )
                    }
                }
            }
        }
    }
}