package nl.jacobras.codeobserver.dashboard.detekt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrieldrn.carbon.notification.NotificationStatus
import com.github.michaelbull.result.get
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import nl.jacobras.codeobserver.dto.DetektMetricDto
import nl.jacobras.codeobserver.projects.ProjectRepository
import nl.jacobras.codeobserver.util.ui.UiState
import nl.jacobras.codeobserver.util.ui.notification.Notifier

@OptIn(ExperimentalCoroutinesApi::class)
internal class DetektTrendsViewModel(
    private val detektReportRepository: DetektReportRepository,
    projectRepository: ProjectRepository
) : ViewModel() {

    private val projectId = projectRepository.selectedProjectId
    val metricsState =
        combine(
            detektReportRepository.metricsLoadingState,
            detektReportRepository.deletingState
        ) { loading, deleting ->
            UiState(
                loading = loading,
                deleting = deleting
            )
        }
    val metrics = MutableStateFlow(emptyList<DetektMetricDto>())

    val detailReportState = detektReportRepository.reportLoadingState.map { UiState<String>(loading = it) }
    val latestReportId = metrics.map { reports -> reports.maxByOrNull { it.gitDate }?.id }
    val detailReport = latestReportId.flatMapLatest { id ->
        val content = id.takeIf { it != null }
            ?.let { detektReportRepository.fetchReport(it) }
            ?.takeIf { it.isOk }
            ?.get() ?: ""
        flowOf(content)
    }

    init {
        refresh()

        viewModelScope.launch {
            projectId.collect { refresh() }
        }
    }

    fun refresh() = viewModelScope.launch {
        val projectId = projectId.value ?: return@launch
        detektReportRepository.fetchMetrics(projectId)
            .onOk { metrics.value = it }
            .onErr {
                Notifier.show(
                    title = "Error loading metrics",
                    message = "Due to $it",
                    status = NotificationStatus.Error
                )
            }
    }

    fun delete(report: DetektMetricDto) = viewModelScope.launch {
        detektReportRepository.deleteReport(reportId = report.id)
            .onOk {
                refresh()
                Notifier.show(
                    title = "Report deleted",
                    status = NotificationStatus.Success
                )
            }
            .onErr {
                Notifier.show(
                    title = "Error deleting report",
                    message = "Due to $it",
                    status = NotificationStatus.Error
                )
            }
    }
}