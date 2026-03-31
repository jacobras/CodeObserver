package nl.jacobras.codeobserver.dashboard.buildtimes

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import nl.jacobras.codeobserver.dto.BuildTimeDto
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.codeobserver.util.data.NetworkError
import nl.jacobras.codeobserver.util.data.RequestState

internal class BuildTimesRepository(
    private val dataSource: BuildTimesDataSource
) {
    val loadingState = MutableStateFlow<RequestState>(RequestState.Idle)

    suspend fun fetchBuildTimes(projectId: ProjectId): Result<List<BuildTimeDto>, NetworkError> {
        loadingState.value = RequestState.Working
        return dataSource.fetchBuildTimes(projectId)
            .onOk { loadingState.value = RequestState.Idle }
            .onErr { loadingState.value = RequestState.Error(it) }
    }
}