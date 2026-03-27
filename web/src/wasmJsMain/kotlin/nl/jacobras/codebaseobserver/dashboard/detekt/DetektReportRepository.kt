package nl.jacobras.codebaseobserver.dashboard.detekt

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import nl.jacobras.codebaseobserver.dto.DetektMetricDto
import nl.jacobras.codebaseobserver.dto.ProjectId
import nl.jacobras.codebaseobserver.dto.ReportId
import nl.jacobras.codebaseobserver.util.data.NetworkError
import nl.jacobras.codebaseobserver.util.data.RequestState

internal class DetektReportRepository(
    private val dataSource: DetektReportDataSource
) {
    val metricsLoadingState = MutableStateFlow<RequestState>(RequestState.Idle)
    val reportLoadingState = MutableStateFlow<RequestState>(RequestState.Idle)
    val deletingState = MutableStateFlow<Map<ReportId, RequestState>>(emptyMap())

    suspend fun fetchMetrics(projectId: ProjectId): Result<List<DetektMetricDto>, NetworkError> {
        metricsLoadingState.value = RequestState.Working
        return dataSource.fetchMetrics(projectId)
            .onOk { metricsLoadingState.value = RequestState.Idle }
            .onErr { metricsLoadingState.value = RequestState.Error(it) }
    }

    suspend fun fetchReport(reportId: ReportId): Result<String, NetworkError> {
        reportLoadingState.value = RequestState.Working
        return dataSource.fetchReport(reportId)
            .onOk { reportLoadingState.value = RequestState.Idle }
            .onErr { reportLoadingState.value = RequestState.Error(it) }
    }

    suspend fun deleteReport(reportId: ReportId): Result<Unit, NetworkError> {
        deletingState.update { it + mapOf(reportId to RequestState.Working) }
        return dataSource.delete(reportId)
            .onOk { deletingState.update { it - reportId } }
            .onErr { error ->
                deletingState.update { it + mapOf(reportId to RequestState.Error(error)) }
            }
    }
}