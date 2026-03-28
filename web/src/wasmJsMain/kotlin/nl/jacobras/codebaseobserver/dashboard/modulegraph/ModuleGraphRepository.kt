package nl.jacobras.codebaseobserver.dashboard.modulegraph

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import nl.jacobras.codebaseobserver.dto.GraphModulesDto
import nl.jacobras.codebaseobserver.dto.ModuleSortOrder
import nl.jacobras.codebaseobserver.dto.ProjectId
import nl.jacobras.codebaseobserver.util.data.NetworkError
import nl.jacobras.codebaseobserver.util.data.RequestState

internal class ModuleGraphRepository(
    private val dataSource: ModuleGraphDataSource
) {
    val loadingState = MutableStateFlow<RequestState>(RequestState.Idle)

    suspend fun fetchGraphModules(
        projectId: ProjectId,
        sortOrder: ModuleSortOrder
    ): Result<GraphModulesDto, NetworkError> {
        loadingState.value = RequestState.Working
        return dataSource.fetchGraphModules(projectId, sortOrder)
            .onOk { loadingState.value = RequestState.Idle }
            .onErr { loadingState.value = RequestState.Error(it) }
    }

    suspend fun fetchGraph(
        projectId: ProjectId,
        startModule: String,
        groupingThreshold: Int,
        layerDepth: Int
    ): Result<String, NetworkError> {
        loadingState.value = RequestState.Working
        return dataSource.fetchGraph(
            projectId = projectId,
            startModule = startModule,
            groupingThreshold = groupingThreshold,
            layerDepth = layerDepth
        )
            .onOk { loadingState.value = RequestState.Idle }
            .onErr { loadingState.value = RequestState.Error(it) }
    }
}