package nl.jacobras.codebaseobserver.dashboard.detekt

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import nl.jacobras.codebaseobserver.dto.DetektMetricDto
import nl.jacobras.codebaseobserver.util.data.NetworkError

internal class DetektReportDataSource(
    private val client: HttpClient
) {
    suspend fun fetchReport(reportId: Int): Result<String, NetworkError> {
        Logger.i("Fetching Detekt report $reportId")
        return runSuspendCatching {
            client.get("/detektReports/$reportId").body<String>()
        }.mapError {
            Logger.e(it) { "Failed to fetch Detekt report" }
            NetworkError.UnknownError
        }
    }

    suspend fun fetchMetrics(projectId: String): Result<List<DetektMetricDto>, NetworkError> {
        Logger.i("Fetching Detekt metrics for project $projectId")
        return runSuspendCatching {
            client.get("/detektMetrics") {
                url { parameters.append("projectId", projectId) }
            }.body<List<DetektMetricDto>>()
        }.mapError {
            Logger.e(it) { "Failed to fetch Detekt metrics" }
            NetworkError.UnknownError
        }
    }

    suspend fun delete(projectId: String, gitHash: String): Result<Unit, NetworkError> {
        Logger.i("Deleting Detekt report for project $projectId and git hash $gitHash")
        return runSuspendCatching {
            client.delete("/detektReports/$projectId/$gitHash")
            Logger.i("Detekt report for project $projectId and git hash $gitHash deleted")
        }.mapError {
            Logger.e(it) { "Failed to delete Detekt report" }
            NetworkError.UnknownError
        }
    }
}