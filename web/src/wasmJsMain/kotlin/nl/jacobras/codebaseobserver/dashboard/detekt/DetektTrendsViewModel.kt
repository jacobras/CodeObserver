package nl.jacobras.codebaseobserver.dashboard.detekt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.get
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import nl.jacobras.codebaseobserver.dto.DetektMetricDto
import nl.jacobras.codebaseobserver.projects.ProjectRepository
import nl.jacobras.codebaseobserver.util.ui.UiState

@OptIn(ExperimentalCoroutinesApi::class)
internal class DetektTrendsViewModel(
    private val detektReportRepository: DetektReportRepository,
    projectRepository: ProjectRepository
) : ViewModel() {

    private val projectId = projectRepository.selectedProjectId
    val metricsState = detektReportRepository.metricsLoadingState.map { UiState<Int>(loading = it) }
    val metrics = MutableStateFlow(emptyList<DetektMetricDto>())

    val detailReportState = detektReportRepository.reportLoadingState.map { UiState<String>(loading = it) }
    val latestReportId = metrics.map { it.firstOrNull()?.id ?: -1 }
    val detailReport = latestReportId.mapNotNull { id ->
        if (id != -1) {
            val res = detektReportRepository.fetchReport(id)
            if (res.isOk) {
                res.get()
            } else {
                ""
            }
        } else {
            ""
        }
    }

    init {
        refresh()

        viewModelScope.launch {
            projectId.collect { refresh() }
        }
        viewModelScope.launch {
            metrics.collect { refresh() }
        }
    }

    fun refresh() = viewModelScope.launch {
        detektReportRepository.fetchMetrics(projectId.value)
            .onOk { metrics.value = it }
    }

    fun delete(record: DetektMetricDto) = viewModelScope.launch {
        detektReportRepository.deleteReport(projectId = record.projectId, gitHash = record.gitHash)
            .onOk { refresh() }
    }
}