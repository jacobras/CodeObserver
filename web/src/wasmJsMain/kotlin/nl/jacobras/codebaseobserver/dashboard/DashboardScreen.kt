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
import nl.jacobras.codebaseobserver.AppViewModel
import nl.jacobras.codebaseobserver.dashboard.artifacts.ArtifactCharts
import nl.jacobras.codebaseobserver.dashboard.buildtimes.BuildTimes
import nl.jacobras.codebaseobserver.dashboard.detekt.DetektReport
import nl.jacobras.codebaseobserver.dashboard.detekt.DetektTrends
import nl.jacobras.codebaseobserver.dashboard.migrations.Migrations
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModuleGraph
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModuleRules
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModuleTypes
import nl.jacobras.codebaseobserver.dashboard.trends.CodeTrends
import nl.jacobras.codebaseobserver.di.RepositoryLocator
import nl.jacobras.codebaseobserver.dto.ProjectDto
import nl.jacobras.codebaseobserver.dto.ProjectId
import nl.jacobras.codebaseobserver.util.ui.chart.TimeView

@Composable
internal fun DashboardScreen() {
    val viewModel = viewModel { AppViewModel(RepositoryLocator.projectRepository) }
    val projects by viewModel.projects.collectAsState(emptyList())
    val selectedProjectId by viewModel.selectedProjectId.collectAsState(null)
    val loadingError by viewModel.loadingError.collectAsState("")

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
    selectedProjectId: ProjectId?,
    onSelectProject: (ProjectId) -> Unit
) {
    Column {
        Dropdown(
            label = "Project",
            placeholder = "Select a project",
            options = projects
                .sortedBy { it.name }
                .associate { project ->
                    project.id to DropdownOption("${project.name} (${project.id.value})")
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
                navController.navigate(destination.route) {
                    launchSingleTop = true
                    popUpTo(navController.graph.startDestinationId)
                }
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
                if (selectedProjectId == null) {
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
                            timeView = timeView,
                            onSelectTimeView = { timeView = it }
                        )
                    }
                    composable(DashboardDestination.Artifacts.route) {
                        ArtifactCharts()
                    }
                    composable(DashboardDestination.BuildTimes.route) {
                        BuildTimes(
                            timeView = timeView,
                            onSelectTimeView = { timeView = it }
                        )
                    }
                    composable(DashboardDestination.DetektTrends.route) {
                        DetektTrends(
                            timeView = timeView,
                            onSelectTimeView = { timeView = it }
                        )
                    }
                    composable(DashboardDestination.DetektReport.route) {
                        DetektReport()
                    }
                    composable(DashboardDestination.Migrations.route) {
                        Migrations(
                            timeView = timeView,
                            onSelectTimeView = { timeView = it }
                        )
                    }
                    composable(DashboardDestination.ModuleGraph.route) {
                        ModuleGraph()
                    }
                    composable(DashboardDestination.ModuleRules.route) {
                        ModuleRules()
                    }
                    composable(DashboardDestination.ModuleTypes.route) {
                        ModuleTypes()
                    }
                }
            }
        }
    }
}