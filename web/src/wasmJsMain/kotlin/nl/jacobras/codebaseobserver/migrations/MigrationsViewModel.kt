package nl.jacobras.codebaseobserver.migrations

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
import nl.jacobras.codebaseobserver.dto.MigrationDto
import nl.jacobras.codebaseobserver.dto.MigrationRequest
import nl.jacobras.codebaseobserver.dto.MigrationUpdateRequest
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
internal class MigrationsViewModel(
    private val client: HttpClient
) : ViewModel() {

    private val projectId = MutableStateFlow("")
    private val refreshKey = MutableStateFlow(0)
    val isLoading = MutableStateFlow(false)
    val loadingError = MutableStateFlow("")
    val updateError = MutableStateFlow("")

    val migrations: Flow<List<MigrationDto>> = combine(projectId, refreshKey) { id, _ -> id }
        .filter { it.isNotEmpty() }
        .flatMapLatest { projectId ->
            try {
                isLoading.value = true
                val list = client.get("/migrations") {
                    url { parameters.append("projectId", projectId) }
                }.body<List<MigrationDto>>()
                isLoading.value = false
                loadingError.value = ""
                flowOf(list)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.e(e) { "Failed to fetch migrations" }
                isLoading.value = false
                loadingError.value = "Failed to fetch migrations: ${e.message}"
                flowOf(emptyList())
            }
        }

    fun setProjectId(id: String) {
        projectId.value = id
    }

    fun refresh() {
        refreshKey.value++
    }

    fun save(id: Int?, name: String, description: String, type: String, rule: String) = viewModelScope.launch {
        try {
            updateError.value = ""
            if (id != null) {
                client.patch("/migrations/$id") {
                    contentType(ContentType.Application.Json)
                    setBody(MigrationUpdateRequest(name = name, description = description))
                }
            } else {
                client.post("/migrations") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        MigrationRequest(
                            projectId = projectId.value,
                            name = name,
                            description = description,
                            type = type,
                            rule = rule
                        )
                    )
                }
            }
            refresh()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Logger.e(e) { "Failed to save migration" }
            updateError.value = "Failed to save migration: ${e.message}"
        }
    }

    fun delete(id: Int) = viewModelScope.launch {
        try {
            updateError.value = ""
            client.delete("/migrations/$id")
            refresh()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Logger.e(e) { "Failed to delete migration" }
            updateError.value = "Failed to delete migration: ${e.message}"
        }
    }
}