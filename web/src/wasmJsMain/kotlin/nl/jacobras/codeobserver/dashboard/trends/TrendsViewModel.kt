package nl.jacobras.codeobserver.dashboard.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrieldrn.carbon.notification.NotificationStatus
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import nl.jacobras.codeobserver.dto.CodeMetricsDto
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.codeobserver.projects.ProjectRepository
import nl.jacobras.codeobserver.util.ui.UiState
import nl.jacobras.codeobserver.util.ui.notification.Notifier

internal class TrendsViewModel(
    private val trendsRepository: TrendsRepository,
    projectRepository: ProjectRepository
) : ViewModel() {

    val projectId = projectRepository.selectedProjectId
    val uiState = combine(
        trendsRepository.loadingState,
        trendsRepository.deletingState
    ) { loading, deleting ->
        UiState(loading = loading, deleting = deleting)
    }
    val metrics = MutableStateFlow(emptyList<CodeMetricsDto>())

    init {
        viewModelScope.launch {
            projectId.collectLatest { id ->
                if (id != null) {
                    loadData(id)
                }
            }
        }
    }

    private suspend fun loadData(projectId: ProjectId) {
        trendsRepository.fetchMetrics(projectId)
            .onOk { metrics.value = it }
            .onErr {
                Notifier.show(
                    title = "Error fetching metrics",
                    message = "Due to $it",
                    status = NotificationStatus.Error
                )
            }
    }

    fun refresh() = viewModelScope.launch {
        val id = projectId.value ?: return@launch
        loadData(id)
    }

    fun delete(record: CodeMetricsDto) = viewModelScope.launch {
        val id = projectId.value ?: return@launch
        trendsRepository.delete(projectId = id, gitHash = record.gitHash)
            .onOk {
                refresh()
                Notifier.show(
                    title = "Metric '${record.gitHash.value}' deleted",
                    status = NotificationStatus.Success
                )
            }
            .onErr {
                Notifier.show(
                    title = "Error deleting metric '${record.gitHash.value}'",
                    message = "Due to $it",
                    status = NotificationStatus.Error
                )
            }
    }
}