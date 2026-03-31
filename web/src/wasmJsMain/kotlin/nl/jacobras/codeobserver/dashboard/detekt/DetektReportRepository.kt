package nl.jacobras.codeobserver.dashboard.detekt

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import nl.jacobras.codeobserver.dto.DetektMetricDto
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.codeobserver.dto.ReportId
import nl.jacobras.codeobserver.util.data.NetworkError
import nl.jacobras.codeobserver.util.data.RequestState

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