package nl.jacobras.codeobserver.dashboard.modulegraph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.gabrieldrn.carbon.notification.NotificationStatus
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.fold
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import nl.jacobras.codeobserver.dashboard.modulegraph.util.GraphConfig
import nl.jacobras.codeobserver.dashboard.modulegraph.util.GraphVisualizer
import nl.jacobras.codeobserver.dto.GitHash
import nl.jacobras.codeobserver.dto.GraphConfigDto
import nl.jacobras.codeobserver.dto.GradleDto
import nl.jacobras.codeobserver.dto.GraphVisualInfoDto
import nl.jacobras.codeobserver.dto.ModuleSortOrder
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.codeobserver.projects.ProjectRepository
import nl.jacobras.codeobserver.util.data.NetworkError
import nl.jacobras.codeobserver.util.ui.UiState
import nl.jacobras.codeobserver.util.ui.notification.Notifier

@OptIn(ExperimentalCoroutinesApi::class)
internal class ModuleGraphViewModel(
    private val modulesRepository: ModuleGraphRepository,
    projectRepository: ProjectRepository
) : ViewModel() {

    val projectId = projectRepository.selectedProjectId
    val sortOrder: StateFlow<ModuleSortOrder>
        field = MutableStateFlow(ModuleSortOrder.Alphabetical)
    val uiState = modulesRepository.loadingState.map { UiState<Nothing>(loading = it) }
    val graphModules: StateFlow<GradleDto>
        field = MutableStateFlow(GradleDto())

    val startModule: StateFlow<String>
        field = MutableStateFlow("")
    val groupingThreshold: StateFlow<Int>
        field = MutableStateFlow(DEFAULT_GROUPING_THRESHOLD)
    val layerDepth: StateFlow<Int>
        field = MutableStateFlow(DEFAULT_LAYER_DEPTH)

    /** Commit to show, or `null` for the latest. Driven by the history slider. */
    val selectedCommit: StateFlow<GitHash?>
        field = MutableStateFlow<GitHash?>(null)

    // In-memory caches so dragging back to an already-loaded commit is instant (no network call).
    private val modulesCache = mutableMapOf<Triple<ProjectId, GitHash?, ModuleSortOrder>, GradleDto>()
    private val graphInfoCache = mutableMapOf<Pair<ProjectId, GitHash?>, GraphVisualInfoDto>()

    val graphInfo = combine(projectId, selectedCommit) { id, commit -> id to commit }
        .mapLatest { (projectId, commit) ->
            if (projectId == null) {
                return@mapLatest GraphVisualInfoDto()
            }
            graphInfoCache[projectId to commit]?.let { return@mapLatest it }
            loadGraphInfo(projectId, commit)
                .fold(
                    success = { info ->
                        graphInfoCache[projectId to commit] = info
                        info
                    },
                    failure = { error ->
                        Logger.e { "Failed to fetch graph info: $error" }
                        Notifier.show(
                            title = "Error loading module graph info",
                            message = "Due to $error",
                            status = NotificationStatus.Error
                        )
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
            moduleColors = graphInfo.moduleColors,
            config = graphInfo.config.map {
                when (it) {
                    is GraphConfigDto.DeprecatedModule -> GraphConfig.DeprecatedModule(it.module)
                    is GraphConfigDto.ForbiddenDependency -> GraphConfig.ForbiddenDependency(it.a, it.b)
                }
            }
        )
    }

    init {
        // Reset to the latest commit whenever the selected project changes.
        viewModelScope.launch {
            projectId.collectLatest { selectedCommit.value = null }
        }
        viewModelScope.launch {
            combine(projectId, sortOrder, selectedCommit) { id, sort, commit -> Triple(id, sort, commit) }
                .collectLatest { (id, _, _) ->
                    if (id != null) {
                        loadData()
                    }
                }
        }
    }

    fun setStartModule(module: String) {
        startModule.value = module
    }

    fun setSelectedCommit(gitHash: GitHash?) {
        selectedCommit.value = gitHash
    }

    fun setSortOrder(order: ModuleSortOrder) {
        sortOrder.value = order
    }

    fun setGroupingThreshold(threshold: Int) {
        groupingThreshold.value = threshold
    }

    fun setLayerDepth(depth: Int) {
        layerDepth.value = depth
    }

    private suspend fun loadData() {
        val projectId = projectId.value ?: return
        val commit = selectedCommit.value
        val sort = sortOrder.value
        val cacheKey = Triple(projectId, commit, sort)
        modulesCache[cacheKey]?.let {
            graphModules.value = it
            return
        }
        modulesRepository.fetchGraphModules(projectId, sort, commit)
            .onOk {
                modulesCache[cacheKey] = it
                graphModules.value = it
            }
    }

    fun refresh() = viewModelScope.launch {
        loadData()
    }

    suspend fun loadGraphInfo(
        projectId: ProjectId,
        gitHash: GitHash?
    ): Result<GraphVisualInfoDto, NetworkError> {
        return modulesRepository.fetchGraphInfo(projectId = projectId, gitHash = gitHash)
    }
}

private const val DEFAULT_GROUPING_THRESHOLD = 3
private const val DEFAULT_LAYER_DEPTH = 1