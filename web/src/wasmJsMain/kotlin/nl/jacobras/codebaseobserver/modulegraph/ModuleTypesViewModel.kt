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
import nl.jacobras.codebaseobserver.dto.ModuleTypeIdentifierDto
import nl.jacobras.codebaseobserver.dto.ModuleTypeIdentifierRequest
import nl.jacobras.codebaseobserver.dto.ModuleTypeIdentifierUpdateRequest
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
internal class ModuleTypesViewModel(
    private val client: HttpClient
) : ViewModel() {

    private val projectId = MutableStateFlow("")
    private val refreshKey = MutableStateFlow(0)
    val isLoading = MutableStateFlow(false)
    val loadingError = MutableStateFlow("")
    val updateError = MutableStateFlow("")

    val moduleTypeIdentifiers: Flow<List<ModuleTypeIdentifierDto>> = combine(projectId, refreshKey) { id, _ -> id }
        .filter { it.isNotEmpty() }
        .flatMapLatest { projectId ->
            try {
                isLoading.value = true
                val list = client.get("/moduleTypeIdentifiers") {
                    url { parameters.append("projectId", projectId) }
                }.body<List<ModuleTypeIdentifierDto>>()
                isLoading.value = false
                loadingError.value = ""
                flowOf(list)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(e) { "Failed to fetch module type identifiers" }
                isLoading.value = false
                loadingError.value = "Failed to fetch module type identifiers: ${e.message}"
                flowOf(emptyList())
            }
        }

    fun setProjectId(id: String) {
        projectId.value = id
    }

    fun refresh() {
        refreshKey.value++
    }

    fun save(id: Int?, typeName: String, plugin: String, order: Int, color: String) = viewModelScope.launch {
        try {
            updateError.value = ""
            if (id != null) {
                client.patch("/moduleTypeIdentifiers/$id") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        ModuleTypeIdentifierUpdateRequest(
                            typeName = typeName,
                            plugin = plugin,
                            order = order,
                            color = color
                        )
                    )
                }
            } else {
                client.post("/moduleTypeIdentifiers") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        ModuleTypeIdentifierRequest(
                            projectId = projectId.value,
                            typeName = typeName,
                            plugin = plugin,
                            order = order,
                            color = color
                        )
                    )
                }
            }
            refresh()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Logger.e(e) { "Failed to save module identifier" }
            updateError.value = "Failed to save module identifier: ${e.message}"
        }
    }

    fun delete(id: Int) = viewModelScope.launch {
        try {
            updateError.value = ""
            client.delete("/moduleTypeIdentifiers/$id")
            refresh()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Logger.e(e) { "Failed to delete module type identifier" }
            updateError.value = "Failed to delete module type identifier: ${e.message}"
        }
    }
}