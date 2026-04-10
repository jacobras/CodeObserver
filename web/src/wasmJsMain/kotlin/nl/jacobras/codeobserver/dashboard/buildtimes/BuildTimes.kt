package nl.jacobras.codeobserver.dashboard.buildtimes

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
import nl.jacobras.codeobserver.di.RepositoryLocator
import nl.jacobras.codeobserver.dto.BuildTimeDto
import nl.jacobras.codeobserver.util.data.RequestState
import nl.jacobras.codeobserver.util.ui.UiState
import nl.jacobras.codeobserver.util.ui.chart.ChartColor
import nl.jacobras.codeobserver.util.ui.chart.TimeChart
import nl.jacobras.codeobserver.util.ui.chart.TimeView
import nl.jacobras.codeobserver.util.ui.chart.TimeViewSelector
import nl.jacobras.codeobserver.util.ui.commandinfo.CommandInfoBox
import nl.jacobras.codeobserver.util.ui.layout.SingleChartWithDataTable
import nl.jacobras.codeobserver.util.ui.progress.ProgressIndicator
import nl.jacobras.codeobserver.util.ui.table.DataTable
import nl.jacobras.codeobserver.util.ui.text.excerpt
import nl.jacobras.humanreadable.HumanReadable
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun BuildTimes(
    timeView: TimeView,
    onSelectTimeView: (TimeView) -> Unit
) {
    val viewModel = viewModel {
        BuildTimesViewModel(
            buildTimesRepository = RepositoryLocator.buildTimesRepository,
            projectRepository = RepositoryLocator.projectRepository
        )
    }
    val uiState by viewModel.uiState.collectAsState(UiState())
    val buildTimes by viewModel.buildTimes.collectAsState(emptyList())
    val isLoading = uiState.loading == RequestState.Working
    val loadingError = uiState.loading.let { (it as? RequestState.Error)?.type?.name } ?: ""

    Column {
        if (isLoading || loadingError.isNotEmpty()) {
            ProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                loading = isLoading,
                error = loadingError,
                onRetry = if (loadingError.isNotEmpty()) {
                    { viewModel.refresh() }
                } else {
                    null
                }
            )
            return
        }

        if (buildTimes.isEmpty()) {
            BasicText(
                modifier = Modifier.fillMaxWidth(),
                text = "No build times found",
                style = Carbon.typography.body02
            )
            return
        }

        val buildNames = buildTimes.map { it.buildName }.distinct()
        var selectedBuild by remember(buildNames) { mutableStateOf(buildNames.first()) }

        if (buildNames.size > 1) {
            val tabs = buildNames.map { TabItem(label = it) }

            Row(verticalAlignment = Alignment.CenterVertically) {
                TabList(
                    tabs = tabs,
                    selectedTab = tabs.firstOrNull { it.label == selectedBuild } ?: tabs.first(),
                    onTabSelected = { tab ->
                        selectedBuild = buildNames.first { it == tab.label }
                    }
                )
                val projectId by viewModel.projectId.collectAsState()
                projectId?.let {
                    Spacer(Modifier.weight(1f))
                    CommandInfoBox(
                        command = "report-build-time --name=myBuildName --time=123",
                        projectId = it
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        TimeViewSelector(
            selected = timeView,
            onSelect = { onSelectTimeView(it) }
        )
        Spacer(Modifier.height(16.dp))

        BuildDetail(
            buildName = selectedBuild,
            buildTimes = buildTimes.filter { it.buildName == selectedBuild },
            timeView = timeView
        )
    }
}

@Composable
private fun BuildDetail(
    buildName: String,
    buildTimes: List<BuildTimeDto>,
    timeView: TimeView
) {
    val buildTimesOldestFirst = buildTimes.sortedBy { it.gitDate }
    val buildTimesNewestFirst = buildTimesOldestFirst.reversed()

    SingleChartWithDataTable(
        modifier = Modifier.fillMaxWidth(),
        chart = { modifier ->
            TimeChart(
                modifier = modifier,
                title = buildName,
                records = buildTimesOldestFirst,
                dateField = { it.gitDate },
                metricField = { it.timeSeconds },
                timeView = timeView,
                color = ChartColor.Amethyst,
                yAxisFormatter = { y -> HumanReadable.duration(y.toLong().seconds) },
            )
        },
        dataTable = { modifier ->
            DataTable(
                modifier = modifier,
                columnHeadings = listOf("Git date", "Git hash", "Time"),
                rowCount = buildTimesNewestFirst.size,
                cellContent = { rowIndex, columnIndex, modifier ->
                    val item = buildTimesNewestFirst[rowIndex]
                    when (columnIndex) {
                        0 -> SelectionContainer(modifier) {
                            BasicText(
                                text = item.gitDate.toString(),
                                style = Carbon.typography.bodyCompact01,
                            )
                        }
                        1 -> SelectionContainer(modifier) {
                            BasicText(
                                text = item.gitHash.excerpt(),
                                style = Carbon.typography.code01,
                            )
                        }
                        2 -> SelectionContainer(modifier) {
                            BasicText(
                                text = HumanReadable.duration(item.timeSeconds.seconds),
                                style = Carbon.typography.bodyCompact01,
                            )
                        }
                    }
                }
            )
        }
    )
}