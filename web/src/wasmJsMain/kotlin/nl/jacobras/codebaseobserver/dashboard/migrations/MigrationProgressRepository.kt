package nl.jacobras.codebaseobserver.dashboard.migrations

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import nl.jacobras.codebaseobserver.dto.MigrationProgressDto
import nl.jacobras.codebaseobserver.util.data.NetworkError
import nl.jacobras.codebaseobserver.util.data.RequestState

internal class MigrationProgressRepository(
    private val dataSource: MigrationProgressDataSource
) {
    val loadingState = MutableStateFlow<RequestState>(RequestState.Idle)

    suspend fun fetchProgress(migrationId: Int): Result<List<MigrationProgressDto>, NetworkError> {
        loadingState.value = RequestState.Working
        return dataSource.fetchProgress(migrationId)
            .onOk { loadingState.value = RequestState.Idle }
            .onErr { loadingState.value = RequestState.Error(it) }
    }
}