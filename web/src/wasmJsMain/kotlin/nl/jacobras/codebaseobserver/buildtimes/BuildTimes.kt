package nl.jacobras.codebaseobserver.buildtimes

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import io.ktor.client.HttpClient
import nl.jacobras.codebaseobserver.ui.chart.ChartColor
import nl.jacobras.codebaseobserver.ui.chart.TimeChart
import nl.jacobras.codebaseobserver.ui.chart.TimeView
import nl.jacobras.codebaseobserver.ui.chart.TimeViewSelector
import nl.jacobras.codebaseobserver.ui.loading.ProgressIndicator
import nl.jacobras.humanreadable.HumanReadable
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun BuildTimes(
    client: HttpClient,
    projectId: String
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

    val firstBuildName = buildTimes.firstOrNull()?.buildName
    if (firstBuildName == null) {
        BasicText(
            modifier = Modifier.fillMaxWidth(),
            text = "No build times found",
            style = Carbon.typography.body02
        )
        return
    }

    val records = buildTimes.filter { it.buildName == firstBuildName }
    var timeView by remember { mutableStateOf(TimeView.Last7Days) }

    TimeViewSelector(
        selected = timeView,
        onSelect = { timeView = it }
    )
    Spacer(Modifier.height(16.dp))

    TimeChart(
        title = firstBuildName,
        records = records,
        dateField = { it.gitDate },
        metricField = { it.timeSeconds },
        timeView = timeView,
        color = ChartColor.Amethyst,
        yAxisFormatter = { y -> HumanReadable.duration(y.toLong().seconds) },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}