package nl.jacobras.codebaseobserver

import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import nl.jacobras.codebaseobserver.dto.ProjectDto
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
internal class AppViewModel(
    private val client: HttpClient
) : ViewModel() {

    private val refreshKey = MutableStateFlow(0)
    val isLoading = MutableStateFlow(false)
    val loadingError = MutableStateFlow("")
    val selectedProjectId = MutableStateFlow("")

    val projects: Flow<List<ProjectDto>> = refreshKey
        .flatMapLatest {
            try {
                isLoading.value = true
                val list = client.get("/projects").body<List<ProjectDto>>()
                isLoading.value = false
                loadingError.value = ""
                val current = selectedProjectId.value
                selectedProjectId.value = when {
                    list.isEmpty() -> ""
                    current.isBlank() -> list.first().projectId
                    list.any { it.projectId == current } -> current
                    else -> list.first().projectId
                }
                flowOf(list)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(e) { "Failed to fetch projects" }
                isLoading.value = false
                loadingError.value = "Failed to fetch projects: ${e.message}"
                flowOf(emptyList())
            }
        }

    fun selectProject(projectId: String) {
        selectedProjectId.value = projectId
    }

    fun refresh() {
        refreshKey.value++
    }
}