package nl.jacobras.codebaseobserver.dashboard.modulegraph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import nl.jacobras.codebaseobserver.dto.ModuleTypeIdentifierDto
import nl.jacobras.codebaseobserver.projects.ProjectRepository
import nl.jacobras.codebaseobserver.util.ui.UiState

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
        refresh()

        viewModelScope.launch {
            projectId.collect { refresh() }
        }
    }

    fun refresh() = viewModelScope.launch {
        moduleTypeIdentifiersRepository.fetchIdentifiers(projectId.value)
            .onOk { moduleTypeIdentifiers.value = it }
    }

    fun save(id: Int?, typeName: String, plugin: String, order: Int, color: String) = viewModelScope.launch {
        moduleTypeIdentifiersRepository.save(id, projectId.value, typeName, plugin, order, color)
            .onOk { refresh() }
    }

    fun delete(id: Int) = viewModelScope.launch {
        moduleTypeIdentifiersRepository.delete(id)
            .onOk { refresh() }
    }
}