package nl.jacobras.codeobserver.dashboard.modulegraph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrieldrn.carbon.notification.NotificationStatus
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import nl.jacobras.codeobserver.dto.ModuleTypeIdentifierDto
import nl.jacobras.codeobserver.dto.ModuleTypeIdentifierId
import nl.jacobras.codeobserver.projects.ProjectRepository
import nl.jacobras.codeobserver.util.ui.UiState
import nl.jacobras.codeobserver.util.ui.notification.Notifier

internal class ModuleTypesViewModel(
    private val moduleTypeIdentifiersRepository: ModuleTypeIdentifiersRepository,
    projectRepository: ProjectRepository
) : ViewModel() {

    private val projectId = projectRepository.selectedProjectId
    val uiState = combine(
        moduleTypeIdentifiersRepository.loadingState,
        moduleTypeIdentifiersRepository.savingState,
        moduleTypeIdentifiersRepository.deletingState
    ) { loading, saving, deleting ->
        UiState(loading = loading, saving = saving, deleting = deleting)
    }
    val moduleTypeIdentifiers = MutableStateFlow(emptyList<ModuleTypeIdentifierDto>())

    init {
        viewModelScope.launch {
            projectId
                .filterNotNull()
                .distinctUntilChanged()
                .collectLatest { refresh() }
        }
    }

    suspend fun loadData() {
        val projectId = projectId.value ?: return
        moduleTypeIdentifiersRepository.fetchIdentifiers(projectId)
            .onOk { moduleTypeIdentifiers.value = it }
            .onErr {
                Notifier.show(
                    title = "Error loading module type identifiers",
                    message = "Due to $it",
                    status = NotificationStatus.Error
                )
            }
    }

    fun refresh() = viewModelScope.launch {
        loadData()
    }

    fun save(
        id: ModuleTypeIdentifierId?,
        typeName: String,
        plugin: String,
        order: Int,
        color: String
    ) = viewModelScope.launch {
        val projectId = projectId.value ?: return@launch
        moduleTypeIdentifiersRepository.save(id, projectId, typeName, plugin, order, color)
            .onOk {
                refresh()
                Notifier.show(
                    title = "Module identifier '$typeName' saved",
                    status = NotificationStatus.Success
                )
            }
            .onErr {
                Notifier.show(
                    title = "Error saving module identifier '$typeName'",
                    message = "Due to $it",
                    status = NotificationStatus.Error
                )
            }
    }

    fun delete(id: ModuleTypeIdentifierId) = viewModelScope.launch {
        moduleTypeIdentifiersRepository.delete(id)
            .onOk {
                refresh()
                Notifier.show(
                    title = "Module identifier deleted",
                    status = NotificationStatus.Success
                )
            }
            .onErr {
                Notifier.show(
                    title = "Error deleting identifier",
                    message = "Due to $it",
                    status = NotificationStatus.Error
                )
            }
    }
}