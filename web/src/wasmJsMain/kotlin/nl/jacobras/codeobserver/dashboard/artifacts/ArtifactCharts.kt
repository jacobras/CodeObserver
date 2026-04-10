package nl.jacobras.codeobserver.dashboard.artifacts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.tab.TabItem
import com.gabrieldrn.carbon.tab.TabList
import io.github.z4kn4fein.semver.toVersion
import nl.jacobras.codeobserver.di.RepositoryLocator
import nl.jacobras.codeobserver.dto.ArtifactSizeDto
import nl.jacobras.codeobserver.util.data.RequestState
import nl.jacobras.codeobserver.util.ui.UiState
import nl.jacobras.codeobserver.util.ui.chart.ChartColor
import nl.jacobras.codeobserver.util.ui.chart.VersionChart
import nl.jacobras.codeobserver.util.ui.commandinfo.CommandInfoBox
import nl.jacobras.codeobserver.util.ui.layout.SingleChartWithDataTable
import nl.jacobras.codeobserver.util.ui.loading.ProgressIndicator
import nl.jacobras.codeobserver.util.ui.table.DataTable
import nl.jacobras.humanreadable.HumanReadable

@Composable
internal fun ArtifactCharts() {
    val viewModel = viewModel {
        ArtifactChartsViewModel(
            artifactSizesRepository = RepositoryLocator.artifactSizesRepository,
            projectRepository = RepositoryLocator.projectRepository
        )
    }
    val artifactSizes by viewModel.artifactSizes.collectAsState(emptyList())
    val state by viewModel.uiState.collectAsState(UiState())

    Column {
        when (val loading = state.loading) {
            is RequestState.Working -> {
                ProgressIndicator(modifier = Modifier.fillMaxWidth(), loading = true)
                return
            }
            is RequestState.Error -> {
                ProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    error = loading.type.name,
                    onRetry = { viewModel.refresh() }
                )
                return
            }
            RequestState.Idle -> Unit
        }

        if (artifactSizes.isEmpty()) {
            BasicText(
                modifier = Modifier.fillMaxWidth(),
                text = "No artifacts found",
                style = Carbon.typography.body02
            )
            return
        }

        val artifacts = artifactSizes.map { it.name }.distinct().sortedBy { it }
        var selectedArtifact by remember { mutableStateOf(artifacts.first()) }

        if (artifacts.size > 1) {
            val tabs = artifacts.map { TabItem(label = it) }

            Row(verticalAlignment = Alignment.CenterVertically) {
                TabList(
                    tabs = tabs,
                    selectedTab = tabs.first { it.label == selectedArtifact },
                    onTabSelected = { tab ->
                        selectedArtifact = artifacts.first { it == tab.label }
                    }
                )
                val projectId by viewModel.projectId.collectAsState()
                projectId?.let {
                    Spacer(Modifier.weight(1f))
                    CommandInfoBox(
                        command = "measure-artifact-size --file=path/to/artifact --name=\"$selectedArtifact\" --semVer=1.2.3",
                        projectId = it
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        ArtifactDetail(
            allArtifactSizes = artifactSizes,
            artifactName = selectedArtifact
        )
    }
}

@Composable
private fun ArtifactDetail(
    allArtifactSizes: List<ArtifactSizeDto>,
    artifactName: String
) {
    val artifactSizesOldestFirst = allArtifactSizes
        .filter { it.name == artifactName }
        .sortedWith { a, b ->
            val a = a.semVer.toVersion()
            val b = b.semVer.toVersion()
            a.compareTo(b)
        }
    val artifactSizesNewestFirst = artifactSizesOldestFirst.reversed()

    SingleChartWithDataTable(
        modifier = Modifier.fillMaxWidth(),
        chart = { modifier ->
            VersionChart(
                modifier = modifier,
                title = "Artifact size",
                records = artifactSizesOldestFirst,
                versionField = { it.semVer.toVersion() },
                metricField = { it.size },
                color = ChartColor.Goldenrod
            )
        }, dataTable = { modifier ->
            DataTable(
                modifier = modifier,
                columnHeadings = listOf("Artifact", "Version", "Size"),
                rowCount = artifactSizesNewestFirst.size,
                cellContent = { rowIndex, columnIndex, modifier ->
                    val item = artifactSizesNewestFirst[rowIndex]
                    when (columnIndex) {
                        0 -> SelectionContainer(modifier) {
                            BasicText(
                                text = item.name,
                                style = Carbon.typography.bodyCompact01
                            )
                        }
                        1 -> SelectionContainer(modifier) {
                            BasicText(
                                text = item.semVer,
                                style = Carbon.typography.code01,
                            )
                        }
                        2 -> SelectionContainer(modifier) {
                            BasicText(
                                text = HumanReadable.fileSize(item.size, decimals = 1),
                                style = Carbon.typography.bodyCompact01,
                            )
                        }
                    }
                }
            )
        }
    )
}