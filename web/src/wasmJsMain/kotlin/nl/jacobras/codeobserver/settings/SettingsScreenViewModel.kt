package nl.jacobras.codeobserver.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrieldrn.carbon.notification.NotificationStatus
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import nl.jacobras.codeobserver.dto.ProjectDto
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.codeobserver.projects.ProjectRepository
import nl.jacobras.codeobserver.util.ui.UiState
import nl.jacobras.codeobserver.util.ui.notification.Notifier

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
            .onErr {
                Notifier.show(
                    title = "Error loading projects",
                    message = "Failed to load projects",
                    status = NotificationStatus.Error
                )
            }
    }

    fun saveProject(projectId: ProjectId, name: String, onSuccess: () -> Unit) = viewModelScope.launch {
        projectRepository.save(
            ProjectDto(
                id = projectId,
                name = name
            )
        ).onOk {
            Notifier.show(
                title = "Project '${projectId.value}' saved",
                status = NotificationStatus.Success
            )
            refresh()
            onSuccess()
        }.onErr {
            Notifier.show(
                title = "Error saving project",
                message = "Due to $it",
                status = NotificationStatus.Error
            )

        }
    }

    fun deleteProject(projectId: ProjectId) = viewModelScope.launch {
        projectRepository.delete(projectId).onOk {
            Notifier.show(
                title = "Project '${projectId.value}' deleted",
                status = NotificationStatus.Success
            )
        }.onErr {
            Notifier.show(
                title = "Error deleting project",
                message = "Due to $it",
                status = NotificationStatus.Error
            )
        }
    }
}