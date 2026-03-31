package nl.jacobras.codeobserver.dashboard.trends

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import nl.jacobras.codeobserver.dto.CodeMetricsDto
import nl.jacobras.codeobserver.dto.GitHash
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.codeobserver.util.data.NetworkError
import nl.jacobras.codeobserver.util.data.RequestState

internal class TrendsRepository(
    private val dataSource: TrendsDataSource
) {
    val loadingState = MutableStateFlow<RequestState>(RequestState.Idle)
    val deletingState = MutableStateFlow<Map<GitHash, RequestState>>(emptyMap())

    suspend fun fetchMetrics(projectId: ProjectId): Result<List<CodeMetricsDto>, NetworkError> {
        loadingState.value = RequestState.Working
        return dataSource.fetchMetrics(projectId)
            .onOk { loadingState.value = RequestState.Idle }
            .onErr { loadingState.value = RequestState.Error(it) }
    }

    suspend fun delete(projectId: ProjectId, gitHash: GitHash): Result<Unit, NetworkError> {
        deletingState.update { it + mapOf(gitHash to RequestState.Working) }
        return dataSource.delete(projectId, gitHash)
            .onOk { deletingState.update { it - gitHash } }
            .onErr { error -> deletingState.update { it + mapOf(gitHash to RequestState.Error(error)) } }
    }
}