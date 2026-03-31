package nl.jacobras.codeobserver.dashboard.artifacts

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import nl.jacobras.codeobserver.dto.ArtifactSizeDto
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.codeobserver.util.data.NetworkError
import nl.jacobras.codeobserver.util.data.RequestState

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