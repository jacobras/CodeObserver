package nl.jacobras.codebaseobserver.dashboard.detekt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import nl.jacobras.codebaseobserver.dto.DetektReportDto
import nl.jacobras.codebaseobserver.projects.ProjectRepository
import nl.jacobras.codebaseobserver.util.ui.UiState

@OptIn(ExperimentalCoroutinesApi::class)
internal class DetektTrendsViewModel(
    private val detektReportRepository: DetektReportRepository,
    projectRepository: ProjectRepository
) : ViewModel() {

    private val projectId = projectRepository.selectedProjectId
    val state = detektReportRepository.loadingState.map { UiState<String>(loading = it) }
    val reports = MutableStateFlow(emptyList<DetektReportDto>())

    init {
        refresh()

        viewModelScope.launch {
            projectId.collect { refresh() }
        }
    }

    fun refresh() = viewModelScope.launch {
        detektReportRepository.fetchReports(projectId.value)
            .onOk { reports.value = it }
    }
}