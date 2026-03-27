package nl.jacobras.codebaseobserver.dashboard.trends

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import nl.jacobras.codebaseobserver.dto.CodeMetricsDto
import nl.jacobras.codebaseobserver.dto.GitHash
import nl.jacobras.codebaseobserver.dto.ProjectId
import nl.jacobras.codebaseobserver.util.data.NetworkError

internal class TrendsDataSource(
    private val client: HttpClient
) {
    suspend fun fetchMetrics(projectId: ProjectId): Result<List<CodeMetricsDto>, NetworkError> {
        Logger.i("Fetching metrics for project ${projectId.value}")
        return runSuspendCatching {
            client.get("/metrics") {
                url { parameters.append("projectId", projectId.value) }
            }.body<List<CodeMetricsDto>>()
        }.mapError {
            Logger.e(it) { "Failed to fetch metrics" }
            NetworkError.UnknownError
        }
    }

    suspend fun delete(projectId: ProjectId, gitHash: GitHash): Result<Unit, NetworkError> {
        Logger.i("Deleting metrics for project ${projectId.value}, git hash ${gitHash.value}")
        return runSuspendCatching {
            client.delete("/metrics/${gitHash.value}") {
                url { parameters.append("projectId", projectId.value) }
            }
            Logger.i("Metrics deleted for git hash ${gitHash.value}")
        }.mapError {
            Logger.e(it) { "Failed to delete metrics" }
            NetworkError.UnknownError
        }
    }
}