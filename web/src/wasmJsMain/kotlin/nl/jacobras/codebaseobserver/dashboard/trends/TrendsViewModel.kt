package nl.jacobras.codebaseobserver.dashboard.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import nl.jacobras.codebaseobserver.dto.CodeMetricsDto
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
                if (id.isNotEmpty()) {
                    loadData()
                }
            }
        }
    }

    private suspend fun loadData() {
        trendsRepository.fetchMetrics(projectId.value)
            .onOk { metrics.value = it }
    }

    fun refresh() = viewModelScope.launch {
        loadData()
    }

    fun delete(record: CodeMetricsDto) = viewModelScope.launch {
        trendsRepository.delete(projectId = projectId.value, gitHash = record.gitHash)
            .onOk { refresh() }
    }
}