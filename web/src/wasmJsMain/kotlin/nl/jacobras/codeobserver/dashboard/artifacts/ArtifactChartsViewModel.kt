package nl.jacobras.codeobserver.dashboard.artifacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import nl.jacobras.codeobserver.dto.ArtifactSizeDto
import nl.jacobras.codeobserver.projects.ProjectRepository
import nl.jacobras.codeobserver.util.ui.UiState

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
                if (id != null) {
                    loadData()
                }
            }
        }
    }

    private suspend fun loadData() {
        val id = projectId.value ?: return
        artifactSizesRepository.fetchArtifactSizes(id)
            .onOk { artifactSizes.value = it }
    }

    fun refresh() = viewModelScope.launch {
        loadData()
    }
}