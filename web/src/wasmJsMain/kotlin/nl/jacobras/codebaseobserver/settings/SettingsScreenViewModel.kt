package nl.jacobras.codebaseobserver.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import nl.jacobras.codebaseobserver.dto.ProjectDto
import nl.jacobras.codebaseobserver.dto.ProjectRequest

@OptIn(ExperimentalCoroutinesApi::class)
internal class SettingsScreenViewModel(
    private val client: HttpClient
) : ViewModel() {

    private val refreshKey = MutableStateFlow(0)
    val isLoading = MutableStateFlow(false)
    val loadingError = MutableStateFlow("")
    val updateError = MutableStateFlow("")

    val projects: Flow<List<ProjectDto>> = refreshKey
        .flatMapLatest {
            try {
                isLoading.value = true
                val list = client.get("/projects").body<List<ProjectDto>>()
                isLoading.value = false
                loadingError.value = ""
                flowOf(list)
            } catch (e: Throwable) {
                Logger.e(e) { "Failed to fetch projects" }
                isLoading.value = false
                loadingError.value = "Failed to fetch projects: ${e.message}"
                flowOf(emptyList())
            }
        }

    fun refresh() {
        refreshKey.value++
    }

    fun saveProject(projectId: String, name: String) = viewModelScope.launch {
        try {
            updateError.value = ""
            client.post("/projects") {
                contentType(ContentType.Application.Json)
                setBody(ProjectRequest(projectId = projectId, name = name))
            }
            refresh()
        } catch (e: Throwable) {
            Logger.e(e) { "Failed to save project" }
            updateError.value = "Failed to save project: ${e.message}"
        }
    }

    fun deleteProject(projectId: String) = viewModelScope.launch {
        try {
            updateError.value = ""
            client.delete("/projects/$projectId")
            refresh()
        } catch (e: Throwable) {
            Logger.e(e) { "Failed to delete project" }
            updateError.value = "Failed to delete project: ${e.message}"
        }
    }
}