package nl.jacobras.codeobserver.dashboard.modulegraph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.fold
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import nl.jacobras.codeobserver.dashboard.modulegraph.util.GraphVisualizer
import nl.jacobras.codeobserver.dto.GraphModulesDto
import nl.jacobras.codeobserver.dto.GraphVisualInfoDto
import nl.jacobras.codeobserver.dto.ModuleSortOrder
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.codeobserver.projects.ProjectRepository
import nl.jacobras.codeobserver.util.data.NetworkError
import nl.jacobras.codeobserver.util.ui.UiState

@OptIn(ExperimentalCoroutinesApi::class)
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
    val graphInfo = projectId.mapLatest { projectId ->
        if (projectId == null) {
            return@mapLatest GraphVisualInfoDto()
        }
        loadGraphInfo(projectId)
            .fold(
                success = { it },
                failure = { error ->
                    Logger.e { "Failed to fetch graph info: $error" }
                    GraphVisualInfoDto()
                }
            )
    }
    val mermaidGraph = combine(
        graphInfo,
        startModule,
        groupingThreshold,
        layerDepth
    ) { graphInfo, startModule, groupingThreshold, layerDepth ->
        GraphVisualizer.build(
            modules = graphInfo.modules,
            startModule = startModule,
            groupingThreshold = groupingThreshold,
            layerDepth = layerDepth,
            moduleColors = graphInfo.moduleColors
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

    suspend fun loadGraphInfo(projectId: ProjectId): Result<GraphVisualInfoDto, NetworkError> {
        return modulesRepository.fetchGraphInfo(projectId = projectId)
    }
}

private const val DEFAULT_GROUPING_THRESHOLD = 3
private const val DEFAULT_LAYER_DEPTH = 30