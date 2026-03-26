package nl.jacobras.codebaseobserver.dashboard.detekt

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import nl.jacobras.codebaseobserver.dto.DetektReportDto
import nl.jacobras.codebaseobserver.util.data.NetworkError
import nl.jacobras.codebaseobserver.util.data.RequestState

internal class DetektReportRepository(
    private val dataSource: DetektReportDataSource
) {
    val loadingState = MutableStateFlow<RequestState>(RequestState.Idle)

    suspend fun fetchReports(projectId: String): Result<List<DetektReportDto>, NetworkError> {
        loadingState.value = RequestState.Working
        return dataSource.fetchMetrics(projectId)
            .onOk { loadingState.value = RequestState.Idle }
            .onErr { loadingState.value = RequestState.Error(it) }
    }
}