package nl.jacobras.codebaseobserver

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import nl.jacobras.codebaseobserver.artifacts.ArtifactCharts
import nl.jacobras.codebaseobserver.buildtimes.BuildTimes
import nl.jacobras.codebaseobserver.di.RepositoryLocator
import nl.jacobras.codebaseobserver.dto.ProjectDto
import nl.jacobras.codebaseobserver.migrations.Migrations
import nl.jacobras.codebaseobserver.modulegraph.DependencyGraph
import nl.jacobras.codebaseobserver.modulegraph.ModuleRules
import nl.jacobras.codebaseobserver.modulegraph.ModuleTypes
import nl.jacobras.codebaseobserver.trends.Trends
import nl.jacobras.codebaseobserver.ui.chart.TimeView

@Composable
internal fun DashboardScreen() {
    val viewModel = viewModel { AppViewModel(RepositoryLocator.projectRepository) }
    val projects by viewModel.projects.collectAsState(emptyList())
    val selectedProjectId by viewModel.selectedProjectId.collectAsState("")
    val loadingError by viewModel.loadingError.collectAsState("")

    // Auto-select or auto-de-select the first project available.
    LaunchedEffect(projects) {
        if (selectedProjectId.isEmpty() && projects.isNotEmpty()) {
            viewModel.selectProject(projects.first().id)
        } else if (selectedProjectId.isNotEmpty() && projects.isEmpty()) {
            viewModel.selectProject("")
        }
    }

    DashboardScreen(
        error = loadingError,
        projects = projects,
        selectedProjectId = selectedProjectId,
        onSelectProject = { viewModel.selectProject(it) }
    )
}

@Composable
private fun DashboardScreen(
    error: String,
    projects: List<ProjectDto>,
    selectedProjectId: String,
    onSelectProject: (String) -> Unit
) {
    val client = remember {
        HttpClient(Js) {
            defaultRequest {
                url("/")
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
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
                    project.id to DropdownOption("${project.name} (${project.id})")
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
                if (error.isNotEmpty()) {
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

                var timeView by remember { mutableStateOf(TimeView.Last7Days) }

                when (selectedTab) {
                    DashboardTab.CodeTrends -> Trends(
                        client = client,
                        projectId = selectedProjectId,
                        timeView = timeView,
                        onSelectTimeView = { timeView = it }
                    )
                    DashboardTab.Artifacts -> ArtifactCharts(
                        client = client,
                        projectId = selectedProjectId
                    )
                    DashboardTab.BuildTimes -> BuildTimes(
                        client = client,
                        projectId = selectedProjectId,
                        timeView = timeView,
                        onSelectTimeView = { timeView = it }
                    )
                    DashboardTab.Migrations -> Migrations(
                        client = client,
                        projectId = selectedProjectId,
                        timeView = timeView,
                        onSelectTimeView = { timeView = it }
                    )
                    DashboardTab.ModuleGraph -> DependencyGraph(
                        client = client,
                        projectId = selectedProjectId
                    )
                    DashboardTab.ModuleRules -> ModuleRules(
                        client = client,
                        projectId = selectedProjectId
                    )
                    DashboardTab.ModuleTypes -> ModuleTypes(
                        client = client,
                        projectId = selectedProjectId
                    )
                }
            }
        }
    }
}