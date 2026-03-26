package nl.jacobras.codebaseobserver.dashboard.modulegraph

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import nl.jacobras.codebaseobserver.dto.GraphModuleDto
import nl.jacobras.codebaseobserver.dto.ModuleSortOrder
import nl.jacobras.codebaseobserver.util.data.NetworkError
import nl.jacobras.codebaseobserver.util.data.RequestState

internal class ModulesRepository(
    private val dataSource: ModulesDataSource
) {
    val loadingState = MutableStateFlow<RequestState>(RequestState.Idle)

    suspend fun fetchModules(projectId: String, sortOrder: ModuleSortOrder): Result<List<GraphModuleDto>, NetworkError> {
        loadingState.value = RequestState.Working
        return dataSource.fetchModules(projectId, sortOrder)
            .onOk { loadingState.value = RequestState.Idle }
            .onErr { loadingState.value = RequestState.Error(it) }
    }
}