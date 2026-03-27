package nl.jacobras.codebaseobserver.dashboard.modulegraph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import nl.jacobras.codebaseobserver.dto.GraphModuleDto
import nl.jacobras.codebaseobserver.dto.ModuleSortOrder
import nl.jacobras.codebaseobserver.projects.ProjectRepository
import nl.jacobras.codebaseobserver.util.ui.UiState

internal class DependencyGraphViewModel(
    private val modulesRepository: ModulesRepository,
    projectRepository: ProjectRepository
) : ViewModel() {

    val projectId: StateFlow<String> = projectRepository.selectedProjectId
    val sortOrder = MutableStateFlow(ModuleSortOrder.Alphabetical)
    val uiState = modulesRepository.loadingState.map { UiState<Nothing>(loading = it) }
    val modules = MutableStateFlow(emptyList<GraphModuleDto>())

    init {
        viewModelScope.launch {
            combine(projectId, sortOrder) { id, sort -> id to sort }
                .collectLatest { (id, _) ->
                    if (id.isNotEmpty()) {
                        loadData()
                    }
                }
        }
    }

    fun setSortOrder(order: ModuleSortOrder) {
        sortOrder.value = order
    }

    private suspend fun loadData() {
        modulesRepository.fetchModules(projectId.value, sortOrder.value)
            .onOk { modules.value = it }
    }

    fun refresh() = viewModelScope.launch {
        loadData()
    }
}