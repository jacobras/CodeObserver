package nl.jacobras.codebaseobserver.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import nl.jacobras.codebaseobserver.dto.ProjectDto
import nl.jacobras.codebaseobserver.projects.ProjectRepository

@OptIn(ExperimentalCoroutinesApi::class)
internal class SettingsScreenViewModel(
    private val projectRepository: ProjectRepository
) : ViewModel() {

    val loadingState = projectRepository.requestState
    val modifyingState = projectRepository.modifyingState
    val projects = projectRepository.projects

    fun refresh() = viewModelScope.launch {
        projectRepository.refresh()
    }

    fun saveProject(projectId: String, name: String) = viewModelScope.launch {
        projectRepository.save(
            ProjectDto(
                id = projectId,
                name = name
            )
        )
        refresh()
    }

    fun deleteProject(projectId: String) = viewModelScope.launch {
        projectRepository.delete(projectId)
    }
}