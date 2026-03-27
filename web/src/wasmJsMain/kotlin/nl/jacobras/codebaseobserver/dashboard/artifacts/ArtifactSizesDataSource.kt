package nl.jacobras.codebaseobserver.dashboard.artifacts

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import nl.jacobras.codebaseobserver.dto.ArtifactSizeDto
import nl.jacobras.codebaseobserver.util.data.NetworkError

internal class ArtifactSizesDataSource(
    private val client: HttpClient
) {
    suspend fun fetchArtifactSizes(projectId: String): Result<List<ArtifactSizeDto>, NetworkError> {
        Logger.i("Fetching artifact sizes for project $projectId")
        return runSuspendCatching {
            client.get("/artifactSizes") {
                url { parameters.append("projectId", projectId) }
            }.body<List<ArtifactSizeDto>>()
        }.mapError {
            Logger.e(it) { "Failed to fetch artifact sizes" }
            NetworkError.UnknownError
        }
    }
}