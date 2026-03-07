package nl.jacobras.codebaseobserver.modulegraph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import nl.jacobras.codebaseobserver.dto.ModuleGraphSettingDto
import nl.jacobras.codebaseobserver.dto.ModuleGraphSettingRequest
import nl.jacobras.codebaseobserver.dto.ModuleGraphSettingUpdateRequest
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
internal class ModuleRulesViewModel(
    private val client: HttpClient
) : ViewModel() {

    private val projectId = MutableStateFlow("")
    private val refreshKey = MutableStateFlow(0)
    val isLoading = MutableStateFlow(false)
    val loadingError = MutableStateFlow("")
    val updateError = MutableStateFlow("")

    val settings: Flow<List<ModuleGraphSettingDto>> = combine(projectId, refreshKey) { id, _ -> id }
        .filter { it.isNotEmpty() }
        .flatMapLatest { projectId ->
            try {
                isLoading.value = true
                val list = client.get("/moduleGraphSettings") {
                    url { parameters.append("projectId", projectId) }
                }.body<List<ModuleGraphSettingDto>>()
                isLoading.value = false
                loadingError.value = ""
                flowOf(list)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(e) { "Failed to fetch module rules" }
                isLoading.value = false
                loadingError.value = "Failed to fetch module rules: ${e.message}"
                flowOf(emptyList())
            }
        }

    fun setProjectId(id: String) {
        projectId.value = id
    }

    fun refresh() {
        refreshKey.value++
    }

    fun save(id: Int?, type: String, data: String) = viewModelScope.launch {
        try {
            updateError.value = ""
            if (id != null) {
                client.patch("/moduleGraphSettings/$id") {
                    contentType(ContentType.Application.Json)
                    setBody(ModuleGraphSettingUpdateRequest(type = type, data = data))
                }
            } else {
                client.post("/moduleGraphSettings") {
                    contentType(ContentType.Application.Json)
                    setBody(ModuleGraphSettingRequest(projectId = projectId.value, type = type, data = data))
                }
            }
            refresh()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Logger.e(e) { "Failed to save setting" }
            updateError.value = "Failed to save setting: ${e.message}"
        }
    }

    fun delete(id: Int) = viewModelScope.launch {
        try {
            updateError.value = ""
            client.delete("/moduleGraphSettings/$id")
            refresh()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Logger.e(e) { "Failed to delete setting" }
            updateError.value = "Failed to delete setting: ${e.message}"
        }
    }
}