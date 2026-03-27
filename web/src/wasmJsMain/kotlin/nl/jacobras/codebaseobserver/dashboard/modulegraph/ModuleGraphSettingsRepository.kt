package nl.jacobras.codebaseobserver.dashboard.modulegraph

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import nl.jacobras.codebaseobserver.dto.ModuleGraphSettingDto
import nl.jacobras.codebaseobserver.dto.ModuleGraphSettingId
import nl.jacobras.codebaseobserver.dto.ProjectId
import nl.jacobras.codebaseobserver.util.data.NetworkError
import nl.jacobras.codebaseobserver.util.data.RequestState

internal class ModuleGraphSettingsRepository(
    private val dataSource: ModuleGraphSettingsDataSource
) {
    val loadingState = MutableStateFlow<RequestState>(RequestState.Idle)
    val savingState = MutableStateFlow<RequestState>(RequestState.Idle)
    val deletingState = MutableStateFlow<Map<ModuleGraphSettingId, RequestState>>(emptyMap())

    suspend fun fetchSettings(projectId: ProjectId): Result<List<ModuleGraphSettingDto>, NetworkError> {
        loadingState.value = RequestState.Working
        return dataSource.fetchSettings(projectId)
            .onOk { loadingState.value = RequestState.Idle }
            .onErr { loadingState.value = RequestState.Error(it) }
    }

    suspend fun save(
        id: ModuleGraphSettingId?,
        projectId: ProjectId,
        type: String,
        data: String
    ): Result<Unit, NetworkError> {
        savingState.value = RequestState.Working
        val result = if (id != null) {
            dataSource.update(id, type, data)
        } else {
            dataSource.create(projectId, type, data)
        }
        return result
            .onOk { savingState.value = RequestState.Idle }
            .onErr { savingState.value = RequestState.Error(it) }
    }

    suspend fun delete(id: ModuleGraphSettingId): Result<Unit, NetworkError> {
        deletingState.update { it + mapOf(id to RequestState.Working) }
        return dataSource.delete(id)
            .onOk { deletingState.update { it - id } }
            .onErr { error -> deletingState.update { it + mapOf(id to RequestState.Error(error)) } }
    }
}