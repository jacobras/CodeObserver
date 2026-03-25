package nl.jacobras.codebaseobserver.dashboard.detekt

import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import nl.jacobras.codebaseobserver.dto.DetektReportDto
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
internal class DetektTrendsViewModel(
    private val client: HttpClient
) : ViewModel() {

    private val projectId = MutableStateFlow("")
    private val refreshKey = MutableStateFlow(0)
    val isLoading = MutableStateFlow(false)
    val loadingError = MutableStateFlow("")

    val reports: Flow<List<DetektReportDto>> = combine(projectId, refreshKey) { id, _ -> id }
        .filter { it.isNotEmpty() }
        .flatMapLatest { projectId ->
            try {
                isLoading.value = true
                val list = client.get("/detektReports") {
                    url { parameters.append("projectId", projectId) }
                }.body<List<DetektReportDto>>()
                loadingError.value = ""
                flowOf(list)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(e) { "Failed to fetch detekt reports" }
                loadingError.value = "Failed to fetch detekt reports: ${e.message}"
                flowOf(emptyList())
            } finally {
                isLoading.value = false
            }
        }

    fun setProjectId(id: String) {
        projectId.value = id
    }

    fun refresh() {
        refreshKey.value++
    }
}