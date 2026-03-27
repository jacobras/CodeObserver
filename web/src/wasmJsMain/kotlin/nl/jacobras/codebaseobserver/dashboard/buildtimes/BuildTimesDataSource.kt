package nl.jacobras.codebaseobserver.dashboard.buildtimes

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import nl.jacobras.codebaseobserver.dto.BuildTimeDto
import nl.jacobras.codebaseobserver.dto.ProjectId
import nl.jacobras.codebaseobserver.util.data.NetworkError

internal interface BuildTimesDataSource {
    suspend fun fetchBuildTimes(projectId: ProjectId): Result<List<BuildTimeDto>, NetworkError>
}

internal class BuildTimesDataSourceImpl(
    private val client: HttpClient
) : BuildTimesDataSource {
    override suspend fun fetchBuildTimes(projectId: ProjectId): Result<List<BuildTimeDto>, NetworkError> {
        Logger.i("Fetching build times for project ${projectId.value}")
        return runSuspendCatching {
            client.get("/buildTimes") {
                url { parameters.append("projectId", projectId.value) }
            }.body<List<BuildTimeDto>>()
        }.mapError {
            Logger.e(it) { "Failed to fetch build times" }
            NetworkError.UnknownError
        }
    }
}