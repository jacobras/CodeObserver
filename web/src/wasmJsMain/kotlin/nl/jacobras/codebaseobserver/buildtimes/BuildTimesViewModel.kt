package nl.jacobras.codebaseobserver.buildtimes

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
import nl.jacobras.codebaseobserver.dto.BuildTimeDto
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
internal class BuildTimesViewModel(
    private val client: HttpClient
) : ViewModel() {

    private val projectId = MutableStateFlow("")
    private val refreshKey = MutableStateFlow(0)
    val isLoading = MutableStateFlow(false)
    val loadingError = MutableStateFlow("")

    val buildTimes: Flow<List<BuildTimeDto>> = combine(projectId, refreshKey) { id, _ -> id }
        .filter { it.isNotEmpty() }
        .flatMapLatest { projectId ->
            try {
                isLoading.value = true
                val list = client.get("/buildTimes") {
                    url { parameters.append("projectId", projectId) }
                }.body<List<BuildTimeDto>>()
                isLoading.value = false
                loadingError.value = ""
                flowOf(list)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(e) { "Failed to fetch build times" }
                isLoading.value = false
                loadingError.value = "Failed to fetch build times: ${e.message}"
                flowOf(emptyList())
            }
        }

    fun setProjectId(id: String) {
        projectId.value = id
    }

    fun refresh() {
        refreshKey.value++
    }
}