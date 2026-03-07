package nl.jacobras.codebaseobserver.migrations

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
import nl.jacobras.codebaseobserver.dto.MigrationProgressDto

@OptIn(ExperimentalCoroutinesApi::class)
internal class MigrationDetailViewModel(
    private val client: HttpClient
) : ViewModel() {

    private val migrationId = MutableStateFlow(0)
    private val refreshKey = MutableStateFlow(0)
    val isLoading = MutableStateFlow(false)
    val loadingError = MutableStateFlow("")

    val progress: Flow<List<MigrationProgressDto>> = combine(migrationId, refreshKey) { id, _ -> id }
        .filter { it > 0 }
        .flatMapLatest { migrationId ->
            try {
                isLoading.value = true
                val list = client.get("/migrationProgress") {
                    url { parameters.append("migrationId", migrationId.toString()) }
                }.body<List<MigrationProgressDto>>()
                isLoading.value = false
                loadingError.value = ""
                flowOf(list)
            } catch (e: Throwable) {
                Logger.e(e) { "Failed to fetch migration progress" }
                isLoading.value = false
                loadingError.value = "Failed to fetch migration progress: ${e.message}"
                flowOf(emptyList())
            }
        }

    fun setMigrationId(id: Int) {
        migrationId.value = id
    }

    fun refresh() {
        refreshKey.value++
    }
}