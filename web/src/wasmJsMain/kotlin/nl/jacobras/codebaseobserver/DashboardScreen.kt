package nl.jacobras.codebaseobserver

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.dropdown.Dropdown
import com.gabrieldrn.carbon.dropdown.base.DropdownInteractiveState
import com.gabrieldrn.carbon.dropdown.base.DropdownOption
import com.gabrieldrn.carbon.foundation.color.CarbonLayer
import com.gabrieldrn.carbon.foundation.color.layerBackground
import com.gabrieldrn.carbon.tab.TabItem
import com.gabrieldrn.carbon.tab.TabList
import com.gabrieldrn.carbon.tab.TabVariant
import io.ktor.client.HttpClient
import nl.jacobras.codebaseobserver.dto.ArtifactSizeDto
import nl.jacobras.codebaseobserver.dto.CodeMetricsDto
import nl.jacobras.codebaseobserver.ui.ArtifactCharts
import nl.jacobras.codebaseobserver.ui.CodeCharts
import nl.jacobras.codebaseobserver.ui.CodeTable
import nl.jacobras.codebaseobserver.ui.DependencyGraph

@Composable
internal fun DashboardScreen(
    metrics: List<CodeMetricsDto>,
    artifactSizes: List<ArtifactSizeDto>,
    error: String?,
    projectIds: List<String>,
    selectedProjectId: String,
    onSelectProject: (String) -> Unit,
    onDelete: (CodeMetricsDto) -> Unit,
    client: HttpClient
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

        var selectedTab by remember { mutableStateOf(DashboardTab.Code) }
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
                if (metrics.isEmpty() && artifactSizes.isEmpty()) {
                    BasicText(
                        text = "No data yet. Submit via the CLI.",
                        style = Carbon.typography.body02
                    )
                } else {
                    when (selectedTab) {
                        DashboardTab.Code -> CodeCharts(
                            metrics = metrics
                        )
                        DashboardTab.CodeData -> CodeTable(
                            metrics = metrics,
                            onDelete = onDelete
                        )
                        DashboardTab.Artifacts -> ArtifactCharts(
                            artifactSizes = artifactSizes
                        )
                        DashboardTab.ModuleGraph -> DependencyGraph(
                            projectId = selectedProjectId,
                            client = client
                        )
                    }
                }
            }
        }
    }
}