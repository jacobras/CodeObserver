package nl.jacobras.codebaseobserver.dashboard.modulegraph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.fold
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import nl.jacobras.codebaseobserver.dto.GraphModulesDto
import nl.jacobras.codebaseobserver.dto.ModuleSortOrder
import nl.jacobras.codebaseobserver.dto.ProjectId
import nl.jacobras.codebaseobserver.projects.ProjectRepository
import nl.jacobras.codebaseobserver.util.data.NetworkError
import nl.jacobras.codebaseobserver.util.ui.UiState

internal class ModuleGraphViewModel(
    private val modulesRepository: ModuleGraphRepository,
    projectRepository: ProjectRepository
) : ViewModel() {

    val projectId = projectRepository.selectedProjectId
    val sortOrder = MutableStateFlow(ModuleSortOrder.Alphabetical)
    val uiState = modulesRepository.loadingState.map { UiState<Nothing>(loading = it) }
    val graphModules = MutableStateFlow(GraphModulesDto())

    val startModule = MutableStateFlow("")
    val groupingThreshold = MutableStateFlow(DEFAULT_GROUPING_THRESHOLD)
    val layerDepth = MutableStateFlow(DEFAULT_LAYER_DEPTH)
    val graph = combine(
        projectId,
        startModule,
        groupingThreshold,
        layerDepth
    ) { projectId, startModule, groupingThreshold, layerDepth ->
        if (projectId == null) {
            return@combine ""
        }
        loadGraph(projectId, startModule, groupingThreshold, layerDepth)
            .fold(
                success = { it },
                failure = { error ->
                    Logger.e { "Failed to fetch graph: $error" }
                    ""
                }
            )
    }

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

    suspend fun loadGraph(
        projectId: ProjectId,
        startModule: String,
        groupingThreshold: Int,
        layerDepth: Int
    ): Result<String, NetworkError> {
        return modulesRepository.fetchGraph(
            projectId = projectId,
            startModule = startModule,
            groupingThreshold = groupingThreshold,
            layerDepth = layerDepth
        )
    }
}

private const val DEFAULT_GROUPING_THRESHOLD = 3
private const val DEFAULT_LAYER_DEPTH = 30