package nl.jacobras.codebaseobserver.dashboard.artifacts

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import nl.jacobras.codebaseobserver.dto.ArtifactSizeDto
import nl.jacobras.codebaseobserver.dto.ProjectId
import nl.jacobras.codebaseobserver.util.data.NetworkError
import nl.jacobras.codebaseobserver.util.data.RequestState

internal class ArtifactSizesRepository(
    private val dataSource: ArtifactSizesDataSource
) {
    val loadingState = MutableStateFlow<RequestState>(RequestState.Idle)

    suspend fun fetchArtifactSizes(projectId: ProjectId): Result<List<ArtifactSizeDto>, NetworkError> {
        loadingState.value = RequestState.Working
        return dataSource.fetchArtifactSizes(projectId)
            .onOk { loadingState.value = RequestState.Idle }
            .onErr { loadingState.value = RequestState.Error(it) }
    }
}