package nl.jacobras.codebaseobserver.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import nl.jacobras.codebaseobserver.dto.ProjectDto
import nl.jacobras.codebaseobserver.projects.ProjectRepository
import nl.jacobras.codebaseobserver.util.ui.UiState

@OptIn(ExperimentalCoroutinesApi::class)
internal class SettingsScreenViewModel(
    private val projectRepository: ProjectRepository
) : ViewModel() {

    val state = combine(
        projectRepository.loadingState,
        projectRepository.savingState,
        projectRepository.deletingState
    ) { loading, saving, deleting ->
        UiState(loading = loading, saving = saving, deleting = deleting)
    }
    val projects = projectRepository.projects

    fun refresh() = viewModelScope.launch {
        projectRepository.refresh()
    }

    fun saveProject(projectId: String, name: String, onSuccess: () -> Unit) = viewModelScope.launch {
        projectRepository.save(
            ProjectDto(
                id = projectId,
                name = name
            )
        )
        onSuccess()
        refresh()
    }

    fun deleteProject(projectId: String) = viewModelScope.launch {
        projectRepository.delete(projectId)
    }
}