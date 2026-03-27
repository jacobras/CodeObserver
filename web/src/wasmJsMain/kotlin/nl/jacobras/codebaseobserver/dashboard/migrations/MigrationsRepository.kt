package nl.jacobras.codebaseobserver.dashboard.migrations

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import nl.jacobras.codebaseobserver.dto.MigrationDto
import nl.jacobras.codebaseobserver.dto.MigrationId
import nl.jacobras.codebaseobserver.dto.ProjectId
import nl.jacobras.codebaseobserver.util.data.NetworkError
import nl.jacobras.codebaseobserver.util.data.RequestState

internal class MigrationsRepository(
    private val dataSource: MigrationsDataSource
) {
    val loadingState = MutableStateFlow<RequestState>(RequestState.Idle)
    val savingState = MutableStateFlow<RequestState>(RequestState.Idle)
    val deletingState = MutableStateFlow<Map<MigrationId, RequestState>>(emptyMap())

    suspend fun fetchMigrations(projectId: ProjectId): Result<List<MigrationDto>, NetworkError> {
        loadingState.value = RequestState.Working
        return dataSource.fetchMigrations(projectId)
            .onOk { loadingState.value = RequestState.Idle }
            .onErr { loadingState.value = RequestState.Error(it) }
    }

    suspend fun save(
        id: MigrationId?,
        projectId: ProjectId,
        name: String,
        description: String,
        type: String,
        rule: String
    ): Result<Unit, NetworkError> {
        savingState.value = RequestState.Working
        val result = if (id != null) {
            dataSource.update(id, name, description)
        } else {
            dataSource.create(projectId, name, description, type, rule)
        }
        return result
            .onOk { savingState.value = RequestState.Idle }
            .onErr { savingState.value = RequestState.Error(it) }
    }

    suspend fun delete(id: MigrationId): Result<Unit, NetworkError> {
        deletingState.update { it + mapOf(id to RequestState.Working) }
        return dataSource.delete(id)
            .onOk { deletingState.update { it - id } }
            .onErr { error -> deletingState.update { it + mapOf(id to RequestState.Error(error)) } }
    }
}