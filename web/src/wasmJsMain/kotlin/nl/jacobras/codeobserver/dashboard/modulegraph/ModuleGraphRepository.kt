package nl.jacobras.codeobserver.dashboard.modulegraph

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import nl.jacobras.codeobserver.dto.GraphModulesDto
import nl.jacobras.codeobserver.dto.ModuleSortOrder
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.codeobserver.util.data.NetworkError
import nl.jacobras.codeobserver.util.data.RequestState

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