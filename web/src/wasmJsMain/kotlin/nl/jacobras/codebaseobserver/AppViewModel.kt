package nl.jacobras.codebaseobserver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import nl.jacobras.codebaseobserver.util.data.RequestState
import nl.jacobras.codebaseobserver.projects.ProjectRepository

@OptIn(ExperimentalCoroutinesApi::class)
internal class AppViewModel(
    private val projectRepository: ProjectRepository
) : ViewModel() {

    val projects = projectRepository.projects
    val selectedProjectId = MutableStateFlow("")

    val loadingError = projectRepository.loadingState.map {
        (it as? RequestState.Error)?.type?.name ?: ""
    }

    fun selectProject(projectId: String) {
        selectedProjectId.value = projectId
    }

    fun refresh() = viewModelScope.launch {
        projectRepository.refresh()
    }
}