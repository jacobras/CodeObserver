package nl.jacobras.codebaseobserver.dashboard.buildtimes

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import nl.jacobras.codebaseobserver.dto.BuildTimeDto
import nl.jacobras.codebaseobserver.util.data.NetworkError

internal class BuildTimesDataSource(
    private val client: HttpClient
) {
    suspend fun fetchBuildTimes(projectId: String): Result<List<BuildTimeDto>, NetworkError> {
        Logger.i("Fetching build times for project $projectId")
        return runSuspendCatching {
            client.get("/buildTimes") {
                url { parameters.append("projectId", projectId) }
            }.body<List<BuildTimeDto>>()
        }.mapError {
            Logger.e(it) { "Failed to fetch build times" }
            NetworkError.UnknownError
        }
    }
}