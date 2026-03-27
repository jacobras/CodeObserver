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
import nl.jacobras.codebaseobserver.dto.ProjectId
import nl.jacobras.codebaseobserver.dto.ReportId
import nl.jacobras.codebaseobserver.util.data.NetworkError

internal class DetektReportDataSource(
    private val client: HttpClient
) {
    suspend fun fetchReport(reportId: ReportId): Result<String, NetworkError> {
        Logger.i("Fetching Detekt report ${reportId.value}")
        return runSuspendCatching {
            client.get("/detektReports/${reportId.value}").body<String>()
        }.mapError {
            Logger.e(it) { "Failed to fetch Detekt report" }
            NetworkError.UnknownError
        }
    }

    suspend fun fetchMetrics(projectId: ProjectId): Result<List<DetektMetricDto>, NetworkError> {
        Logger.i("Fetching Detekt metrics for project ${projectId.value}")
        return runSuspendCatching {
            client.get("/detektMetrics") {
                url { parameters.append("projectId", projectId.value) }
            }.body<List<DetektMetricDto>>()
        }.mapError {
            Logger.e(it) { "Failed to fetch Detekt metrics" }
            NetworkError.UnknownError
        }
    }

    suspend fun delete(reportId: ReportId): Result<Unit, NetworkError> {
        Logger.i("Deleting Detekt report for report ID ${reportId.value}")
        return runSuspendCatching {
            client.delete("/detektReports/${reportId.value}")
            Logger.i("Detekt report for report ID ${reportId.value} deleted")
        }.mapError {
            Logger.e(it) { "Failed to delete Detekt report" }
            NetworkError.UnknownError
        }
    }
}