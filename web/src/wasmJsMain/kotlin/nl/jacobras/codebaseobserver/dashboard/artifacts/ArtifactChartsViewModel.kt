package nl.jacobras.codebaseobserver.dashboard.artifacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import nl.jacobras.codebaseobserver.dto.ArtifactSizeDto
import nl.jacobras.codebaseobserver.projects.ProjectRepository
import nl.jacobras.codebaseobserver.util.ui.UiState

internal class ArtifactChartsViewModel(
    private val artifactSizesRepository: ArtifactSizesRepository,
    projectRepository: ProjectRepository
) : ViewModel() {

    private val projectId = projectRepository.selectedProjectId
    val uiState = artifactSizesRepository.loadingState.map { UiState<Nothing>(loading = it) }
    val artifactSizes = MutableStateFlow(emptyList<ArtifactSizeDto>())

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
        artifactSizesRepository.fetchArtifactSizes(projectId.value)
            .onOk { artifactSizes.value = it }
    }

    fun refresh() = viewModelScope.launch {
        loadData()
    }
}