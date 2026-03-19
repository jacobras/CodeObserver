package nl.jacobras.codebaseobserver.buildtimes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
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
import com.gabrieldrn.carbon.tab.TabItem
import com.gabrieldrn.carbon.tab.TabList
import io.ktor.client.HttpClient
import nl.jacobras.codebaseobserver.dto.BuildTimeDto
import nl.jacobras.codebaseobserver.ui.chart.ChartColor
import nl.jacobras.codebaseobserver.ui.chart.TimeChart
import nl.jacobras.codebaseobserver.ui.chart.TimeView
import nl.jacobras.codebaseobserver.ui.chart.TimeViewSelector
import nl.jacobras.codebaseobserver.ui.loading.ProgressIndicator
import nl.jacobras.codebaseobserver.ui.table.DataTable
import nl.jacobras.humanreadable.HumanReadable
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun BuildTimes(
    client: HttpClient,
    projectId: String,
    timeView: TimeView,
    onSelectTimeView: (TimeView) -> Unit
) {
    val viewModel = viewModel { BuildTimesViewModel(client) }
    val buildTimes by viewModel.buildTimes.collectAsState(emptyList())
    val isLoading by viewModel.isLoading.collectAsState(false)
    val loadingError by viewModel.loadingError.collectAsState("")

    LaunchedEffect(projectId) {
        viewModel.setProjectId(projectId)
    }

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
    var selectedBuild by remember { mutableStateOf(buildNames.first()) }

    if (buildNames.size > 1) {
        val tabs = buildNames.map { TabItem(label = it) }

        TabList(
            tabs = tabs,
            selectedTab = tabs.first { it.label == selectedBuild },
            onTabSelected = { tab ->
                selectedBuild = buildNames.first { it == tab.label }
            }
        )
        Spacer(Modifier.height(16.dp))
    }

    TimeViewSelector(
        selected = timeView,
        onSelect = onSelectTimeView
    )
    Spacer(Modifier.height(16.dp))

    BuildDetail(
        buildTimes = buildTimes.filter { it.buildName == selectedBuild },
        timeView = timeView
    )
}

@Composable
private fun BuildDetail(
    buildTimes: List<BuildTimeDto>,
    timeView: TimeView
) {
    val buildTimesOldestFirst = buildTimes.sortedBy { it.gitDate }
    val buildTimesNewestFirst = buildTimesOldestFirst.reversed()

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        TimeChart(
            modifier = Modifier.weight(1f),
            title = "Build time",
            records = buildTimesOldestFirst,
            dateField = { it.gitDate },
            metricField = { it.timeSeconds },
            timeView = timeView,
            color = ChartColor.Amethyst,
            yAxisFormatter = { y -> HumanReadable.duration(y.toLong().seconds) },
        )
        DataTable(
            modifier = Modifier.weight(1f),
            columnHeadings = listOf("Git hash", "Date", "Time"),
            rowCount = buildTimesNewestFirst.size,
            cellContent = { rowIndex, columnIndex, modifier ->
                val item = buildTimesNewestFirst[rowIndex]
                when (columnIndex) {
                    0 -> SelectionContainer(modifier) {
                        BasicText(
                            text = item.gitHash.take(7),
                            style = Carbon.typography.code01
                        )
                    }
                    1 -> SelectionContainer(modifier) {
                        BasicText(
                            text = item.gitDate.toString(),
                            style = Carbon.typography.bodyCompact01,
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
}