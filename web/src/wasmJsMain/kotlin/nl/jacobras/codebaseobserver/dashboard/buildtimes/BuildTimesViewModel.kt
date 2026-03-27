package nl.jacobras.codebaseobserver.dashboard.buildtimes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import nl.jacobras.codebaseobserver.dto.BuildTimeDto
import nl.jacobras.codebaseobserver.projects.ProjectRepository
import nl.jacobras.codebaseobserver.util.ui.UiState

internal class BuildTimesViewModel(
    private val buildTimesRepository: BuildTimesRepository,
    projectRepository: ProjectRepository
) : ViewModel() {

    private val projectId = projectRepository.selectedProjectId
    val uiState = buildTimesRepository.loadingState.map { UiState<Nothing>(loading = it) }
    val buildTimes = MutableStateFlow(emptyList<BuildTimeDto>())

    init {
        viewModelScope.launch {
            projectId
                .filter { it.isNotBlank() }
                .distinctUntilChanged()
                .collectLatest { refresh() }
        }
    }

    private suspend fun loadData() {
        buildTimesRepository.fetchBuildTimes(projectId.value)
            .onOk { buildTimes.value = it }
    }

    fun refresh() = viewModelScope.launch {
        loadData()
    }
}