package nl.jacobras.codebaseobserver.dashboard

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
import nl.jacobras.codebaseobserver.AppViewModel
import nl.jacobras.codebaseobserver.dashboard.artifacts.ArtifactCharts
import nl.jacobras.codebaseobserver.dashboard.buildtimes.BuildTimes
import nl.jacobras.codebaseobserver.dashboard.migrations.Migrations
import nl.jacobras.codebaseobserver.dashboard.modulegraph.DependencyGraph
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModuleRules
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModuleTypes
import nl.jacobras.codebaseobserver.dashboard.trends.CodeTrends
import nl.jacobras.codebaseobserver.di.RepositoryLocator
import nl.jacobras.codebaseobserver.dto.ProjectDto
import nl.jacobras.codebaseobserver.util.ui.chart.TimeView

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
    DisposableEffect(client) {
        onDispose { client.close() }
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

        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val selectedTab = navBackStackEntry?.destination?.route
            ?.let { DashboardDestination.fromRoute(it) } ?: DashboardDestination.CodeTrends
        val tabs = DashboardDestination.entries.map { TabItem(label = it.label) }
        TabList(
            tabs = tabs,
            variant = TabVariant.Contained,
            selectedTab = tabs.first { it.label == selectedTab.label },
            onTabSelected = { tab ->
                val destination = DashboardDestination.fromLabel(tab.label)
                    ?: error("Cannot find destination for ${tab.label}")
                navController.navigate(destination.route)
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

                NavHost(
                    navController = navController,
                    startDestination = DashboardDestination.CodeTrends.route,
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None }
                ) {
                    composable(DashboardDestination.CodeTrends.route) {
                        CodeTrends(
                            client = client,
                            projectId = selectedProjectId,
                            timeView = timeView,
                            onSelectTimeView = { timeView = it }
                        )
                    }
                    composable(DashboardDestination.Artifacts.route) {
                        ArtifactCharts(
                            client = client,
                            projectId = selectedProjectId
                        )
                    }
                    composable(DashboardDestination.BuildTimes.route) {
                        BuildTimes(
                            client = client,
                            projectId = selectedProjectId,
                            timeView = timeView,
                            onSelectTimeView = { timeView = it }
                        )
                    }
                    composable(DashboardDestination.Migrations.route) {
                        Migrations(
                            client = client,
                            projectId = selectedProjectId,
                            timeView = timeView,
                            onSelectTimeView = { timeView = it }
                        )
                    }
                    composable(DashboardDestination.ModuleGraph.route) {
                        DependencyGraph(
                            client = client,
                            projectId = selectedProjectId
                        )
                    }
                    composable(DashboardDestination.ModuleRules.route) {
                        ModuleRules(
                            client = client,
                            projectId = selectedProjectId
                        )
                    }
                    composable(DashboardDestination.ModuleTypes.route) {
                        ModuleTypes(
                            client = client,
                            projectId = selectedProjectId
                        )
                    }
                }
            }
        }
    }
}