package nl.jacobras.codebaseobserver.dashboard.detekt

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import nl.jacobras.codebaseobserver.dto.DetektReportDto
import nl.jacobras.codebaseobserver.util.data.NetworkError

internal class DetektReportDataSource(
    private val client: HttpClient
) {
    suspend fun fetchMetrics(projectId: String): Result<List<DetektReportDto>, NetworkError> {
        Logger.i("Fetching Detekt metrics for project $projectId")
        return runSuspendCatching {
            client.get("/detektReports") {
                url { parameters.append("projectId", projectId) }
            }.body<List<DetektReportDto>>()
        }.mapError {
            Logger.e(it) { "Failed to fetch Detekt metrics" }
            NetworkError.UnknownError
        }
    }
}