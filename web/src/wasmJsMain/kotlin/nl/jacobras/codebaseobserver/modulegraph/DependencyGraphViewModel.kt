package nl.jacobras.codebaseobserver.modulegraph

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
import nl.jacobras.codebaseobserver.dto.GraphModuleDto
import nl.jacobras.codebaseobserver.dto.ModuleSortOrder
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
internal class DependencyGraphViewModel(
    private val client: HttpClient
) : ViewModel() {

    private val projectId = MutableStateFlow("")
    private val refreshKey = MutableStateFlow(0)
    val isLoading = MutableStateFlow(false)
    val loadingError = MutableStateFlow("")
    val sortOrder = MutableStateFlow(ModuleSortOrder.Alphabetical)

    val modules: Flow<List<GraphModuleDto>> = combine(projectId, refreshKey, sortOrder) { id, _, sort -> id to sort }
        .filter { it.first.isNotEmpty() }
        .flatMapLatest { (projectId, sortOrder) ->
            try {
                isLoading.value = true
                val list = client.get("/modules") {
                    url {
                        parameters.append("projectId", projectId)
                        parameters.append("sort", sortOrder.id)
                    }
                }.body<List<GraphModuleDto>>()
                isLoading.value = false
                loadingError.value = ""
                flowOf(list)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(e) { "Failed to load modules" }
                isLoading.value = false
                loadingError.value = "Failed to load modules: ${e.message}"
                flowOf(emptyList())
            }
        }

    fun setProjectId(id: String) {
        projectId.value = id
    }

    fun setSortOrder(order: ModuleSortOrder) {
        sortOrder.value = order
    }

    fun refresh() {
        refreshKey.value++
    }
}