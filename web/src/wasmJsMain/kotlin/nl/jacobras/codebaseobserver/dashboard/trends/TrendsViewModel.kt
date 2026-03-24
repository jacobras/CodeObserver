package nl.jacobras.codebaseobserver.dashboard.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import nl.jacobras.codebaseobserver.dto.CodeMetricsDto
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
internal class TrendsViewModel(
    private val client: HttpClient
) : ViewModel() {

    private val projectId = MutableStateFlow("")
    private val refreshKey = MutableStateFlow(0)
    val isLoading = MutableStateFlow(false)
    val loadingError = MutableStateFlow("")
    val updateError = MutableStateFlow("")

    val metrics: Flow<List<CodeMetricsDto>> = combine(projectId, refreshKey) { id, _ -> id }
        .filter { it.isNotEmpty() }
        .flatMapLatest { projectId ->
            try {
                isLoading.value = true
                val res = client.get("/metrics") {
                    url { parameters.append("projectId", projectId) }
                }
                val list = res.body<List<CodeMetricsDto>>()
                isLoading.value = false
                loadingError.value = ""
                flowOf(list)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(e) { "Failed to fetch metrics" }
                isLoading.value = false
                loadingError.value = "Failed to fetch metrics: ${e.message}"
                flowOf(emptyList())
            }
        }

    fun setProjectId(projectId: String) {
        this.projectId.value = projectId
    }

    fun delete(record: CodeMetricsDto) = viewModelScope.launch {
        try {
            updateError.value = ""
            client.delete("/metrics/${record.gitHash}") {
                url { parameters.append("projectId", projectId.value) }
            }
            refresh()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Logger.e(e) { "Failed to delete metrics" }
            updateError.value = "Failed to delete metrics: ${e.message}"
        }
    }

    fun refresh() {
        refreshKey.value++
    }
}