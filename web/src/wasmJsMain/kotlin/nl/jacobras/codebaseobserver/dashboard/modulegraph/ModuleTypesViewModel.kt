package nl.jacobras.codebaseobserver.dashboard.modulegraph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import nl.jacobras.codebaseobserver.dto.ModuleTypeIdentifierDto
import nl.jacobras.codebaseobserver.dto.ModuleTypeIdentifierId
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
            .onOk { refresh() }
    }

    fun delete(id: ModuleTypeIdentifierId) = viewModelScope.launch {
        moduleTypeIdentifiersRepository.delete(id)
            .onOk { refresh() }
    }
}