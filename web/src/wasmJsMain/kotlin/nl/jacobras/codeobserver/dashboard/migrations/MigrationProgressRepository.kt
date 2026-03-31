package nl.jacobras.codeobserver.dashboard.migrations

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import nl.jacobras.codeobserver.dto.MigrationId
import nl.jacobras.codeobserver.dto.MigrationProgressDto
import nl.jacobras.codeobserver.util.data.NetworkError
import nl.jacobras.codeobserver.util.data.RequestState

internal class MigrationProgressRepository(
    private val dataSource: MigrationProgressDataSource
) {
    val loadingState = MutableStateFlow<RequestState>(RequestState.Idle)

    suspend fun fetchProgress(migrationId: MigrationId): Result<List<MigrationProgressDto>, NetworkError> {
        loadingState.value = RequestState.Working
        return dataSource.fetchProgress(migrationId)
            .onOk { loadingState.value = RequestState.Idle }
            .onErr { loadingState.value = RequestState.Error(it) }
    }
}