package nl.jacobras.codebaseobserver.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import nl.jacobras.codebaseobserver.dto.CodeMetricsDto

@OptIn(ExperimentalCoroutinesApi::class)
internal class TrendsViewModel(
    private val client: HttpClient
) : ViewModel() {

    private val projectId = MutableStateFlow("")
    private val refreshKey = MutableStateFlow(0)

    val metrics: Flow<List<CodeMetricsDto>> = projectId
        .filterNotNull()
        .flatMapLatest { projectId ->
            val res = client.get("/metrics") {
                url { parameters.append("projectId", projectId) }
            }
            flowOf(res.body<List<CodeMetricsDto>>())
        }

    fun setProjectId(projectId: String) {
        this.projectId.value = projectId
    }

    fun delete(record: CodeMetricsDto) = viewModelScope.launch {
        client.delete("/metrics/${record.gitHash}") {
            url { parameters.append("projectId", projectId.value) }
        }
        refresh()
    }

    private fun refresh() {
        refreshKey.value++
    }
}