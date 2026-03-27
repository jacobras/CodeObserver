package nl.jacobras.codebaseobserver.dashboard.modulegraph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import nl.jacobras.codebaseobserver.dto.GraphModulesDto
import nl.jacobras.codebaseobserver.dto.ModuleSortOrder
import nl.jacobras.codebaseobserver.projects.ProjectRepository
import nl.jacobras.codebaseobserver.util.ui.UiState

internal class ModuleGraphViewModel(
    private val modulesRepository: ModulesRepository,
    projectRepository: ProjectRepository
) : ViewModel() {

    val projectId = projectRepository.selectedProjectId
    val sortOrder = MutableStateFlow(ModuleSortOrder.Alphabetical)
    val uiState = modulesRepository.loadingState.map { UiState<Nothing>(loading = it) }
    val graphModules = MutableStateFlow(GraphModulesDto())

    init {
        viewModelScope.launch {
            combine(projectId, sortOrder) { id, sort -> id to sort }
                .collectLatest { (id, _) ->
                    if (id != null) {
                        loadData()
                    }
                }
        }
    }

    fun setSortOrder(order: ModuleSortOrder) {
        sortOrder.value = order
    }

    private suspend fun loadData() {
        val projectId = projectId.value ?: return
        modulesRepository.fetchGraphModules(projectId, sortOrder.value)
            .onOk { graphModules.value = it }
    }

    fun refresh() = viewModelScope.launch {
        loadData()
    }
}