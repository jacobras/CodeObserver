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
import com.gabrieldrn.carbon.dropdown.Dropdown
import com.gabrieldrn.carbon.dropdown.base.DropdownInteractiveState
import com.gabrieldrn.carbon.dropdown.base.DropdownOption
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
        Dropdown(
            label = "Project",
            placeholder = "Select a project",
            options = projectIds.associateWith {
                DropdownOption(it)
            },
            selectedOption = selectedProjectId,
            onOptionSelected = { onSelectProject(it) },
            isInlined = true,
            state = if (projectIds.isNotEmpty()) DropdownInteractiveState.Enabled else DropdownInteractiveState.Warning(
                "No projects yet. Create one by submitting data via the CLI."
            )
        )
        Spacer(Modifier.height(16.dp))

        var selectedTab by remember { mutableStateOf(DashboardTab.Overview) }
        val tabs = DashboardTab.entries.map { TabItem(label = it.displayName) }
        TabList(
            tabs = tabs,
            variant = TabVariant.Contained,
            selectedTab = tabs.first { it.label == selectedTab.displayName },
            onTabSelected = { tab ->
                selectedTab = DashboardTab.entries.first { it.displayName == tab.label }
            }
        )

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
                    when (selectedTab) {
                        DashboardTab.Overview -> Overview(
                            records = records
                        )
                        DashboardTab.Data -> RecordsTable(
                            records = records,
                            onDelete = onDelete
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Overview(
    records: List<MetricsDto>
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
        Chart(
            title = "Lines of code",
            records = records,
            dateField = { it.gitDate },
            metricField = { it.linesOfCode },
            timeView = timeView,
            color = Color(0xFF2A9D8F),
            modifier = Modifier
                .weight(1f)
                .height(240.dp)
        )
        Chart(
            title = "Module count",
            records = records,
            dateField = { it.gitDate },
            metricField = { it.moduleCount },
            timeView = timeView,
            color = Color(0xFFE76F51),
            modifier = Modifier
                .weight(1f)
                .height(240.dp)
        )
        Chart(
            title = "Module tree height",
            records = records,
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