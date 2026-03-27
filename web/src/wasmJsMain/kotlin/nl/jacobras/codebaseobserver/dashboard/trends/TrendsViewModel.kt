package nl.jacobras.codebaseobserver.dashboard.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import nl.jacobras.codebaseobserver.dto.CodeMetricsDto
import nl.jacobras.codebaseobserver.dto.ProjectId
import nl.jacobras.codebaseobserver.projects.ProjectRepository
import nl.jacobras.codebaseobserver.util.ui.UiState

internal class TrendsViewModel(
    private val trendsRepository: TrendsRepository,
    projectRepository: ProjectRepository
) : ViewModel() {

    private val projectId = projectRepository.selectedProjectId
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
    }

    fun refresh() = viewModelScope.launch {
        val id = projectId.value ?: return@launch
        loadData(id)
    }

    fun delete(record: CodeMetricsDto) = viewModelScope.launch {
        val id = projectId.value ?: return@launch
        trendsRepository.delete(projectId = id, gitHash = record.gitHash)
            .onOk { refresh() }
    }
}