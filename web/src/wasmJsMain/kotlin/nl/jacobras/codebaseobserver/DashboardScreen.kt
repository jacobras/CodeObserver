package nl.jacobras.codebaseobserver

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import nl.jacobras.codebaseobserver.artifacts.ArtifactCharts
import nl.jacobras.codebaseobserver.dto.ProjectDto
import nl.jacobras.codebaseobserver.migrations.Migrations
import nl.jacobras.codebaseobserver.modulegraph.DependencyGraph
import nl.jacobras.codebaseobserver.modulegraph.ModuleRules
import nl.jacobras.codebaseobserver.trends.Trends

@Composable
internal fun DashboardScreen(
    error: String?,
    projects: List<ProjectDto>,
    selectedProjectId: String,
    onSelectProject: (String) -> Unit,
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
            options = projects
                .sortedBy { it.name }
                .associate { project ->
                    project.projectId to DropdownOption("${project.name} (${project.projectId})")
                },
            selectedOption = selectedProjectId,
            onOptionSelected = { onSelectProject(it) },
            isInlined = true,
            state = if (projects.isNotEmpty()) {
                DropdownInteractiveState.Enabled
            } else {
                DropdownInteractiveState.Warning("No projects yet. Create one in Settings.")
            }
        )
        Spacer(Modifier.height(16.dp))

        var selectedTab by remember { mutableStateOf(DashboardTab.CodeTrends) }
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
                if (selectedProjectId.isEmpty()) {
                    BasicText(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Select a project to see the dashboard",
                        style = Carbon.typography.body02
                    )
                    return@Column
                }

                when (selectedTab) {
                    DashboardTab.CodeTrends -> Trends(
                        client = client,
                        projectId = selectedProjectId
                    )
                    DashboardTab.Artifacts -> ArtifactCharts(
                        client = client,
                        projectId = selectedProjectId
                    )
                    DashboardTab.Migrations -> Migrations(
                        client = client,
                        projectId = selectedProjectId
                    )
                    DashboardTab.ModuleGraph -> DependencyGraph(
                        client = client,
                        projectId = selectedProjectId
                    )
                    DashboardTab.ModuleRules -> ModuleRules(
                        client = client,
                        projectId = selectedProjectId
                    )
                }
            }
        }
    }
}