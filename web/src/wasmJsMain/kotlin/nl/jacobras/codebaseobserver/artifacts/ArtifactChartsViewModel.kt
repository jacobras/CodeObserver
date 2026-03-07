package nl.jacobras.codebaseobserver.artifacts

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
import nl.jacobras.codebaseobserver.dto.ArtifactSizeDto

@OptIn(ExperimentalCoroutinesApi::class)
internal class ArtifactChartsViewModel(
    private val client: HttpClient
) : ViewModel() {

    private val projectId = MutableStateFlow("")
    private val refreshKey = MutableStateFlow(0)
    val isLoading = MutableStateFlow(false)
    val loadingError = MutableStateFlow("")

    val artifactSizes: Flow<List<ArtifactSizeDto>> = combine(projectId, refreshKey) { id, _ -> id }
        .filter { it.isNotEmpty() }
        .flatMapLatest { projectId ->
            try {
                isLoading.value = true
                val list = client.get("/artifactSizes") {
                    url { parameters.append("projectId", projectId) }
                }.body<List<ArtifactSizeDto>>()
                isLoading.value = false
                loadingError.value = ""
                flowOf(list)
            } catch (e: Throwable) {
                Logger.e(e) { "Failed to fetch artifact sizes" }
                isLoading.value = false
                loadingError.value = "Failed to fetch artifact sizes: ${e.message}"
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