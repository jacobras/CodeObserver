package nl.jacobras.codebaseobserver.dashboard.modulegraph

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import nl.jacobras.codebaseobserver.dto.ModuleTypeIdentifierDto
import nl.jacobras.codebaseobserver.util.data.NetworkError
import nl.jacobras.codebaseobserver.util.data.RequestState

internal class ModuleTypeIdentifiersRepository(
    private val dataSource: ModuleTypeIdentifiersDataSource
) {
    val loadingState = MutableStateFlow<RequestState>(RequestState.Idle)
    val savingState = MutableStateFlow<RequestState>(RequestState.Idle)
    val deletingState = MutableStateFlow<Map<Int, RequestState>>(emptyMap())

    suspend fun fetchIdentifiers(projectId: String): Result<List<ModuleTypeIdentifierDto>, NetworkError> {
        loadingState.value = RequestState.Working
        return dataSource.fetchIdentifiers(projectId)
            .onOk { loadingState.value = RequestState.Idle }
            .onErr { loadingState.value = RequestState.Error(it) }
    }

    suspend fun save(
        id: Int?,
        projectId: String,
        typeName: String,
        plugin: String,
        order: Int,
        color: String
    ): Result<Unit, NetworkError> {
        savingState.value = RequestState.Working
        val result = if (id != null) {
            dataSource.update(id, typeName, plugin, order, color)
        } else {
            dataSource.create(projectId, typeName, plugin, order, color)
        }
        return result
            .onOk { savingState.value = RequestState.Idle }
            .onErr { savingState.value = RequestState.Error(it) }
    }

    suspend fun delete(id: Int): Result<Unit, NetworkError> {
        deletingState.update { it + mapOf(id to RequestState.Working) }
        return dataSource.delete(id)
            .onOk { deletingState.update { it - id } }
            .onErr { error -> deletingState.update { it + mapOf(id to RequestState.Error(error)) } }
    }
}