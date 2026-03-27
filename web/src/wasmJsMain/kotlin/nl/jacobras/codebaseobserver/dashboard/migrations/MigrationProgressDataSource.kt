package nl.jacobras.codebaseobserver.dashboard.migrations

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import nl.jacobras.codebaseobserver.dto.MigrationProgressDto
import nl.jacobras.codebaseobserver.util.data.NetworkError

internal class MigrationProgressDataSource(
    private val client: HttpClient
) {
    suspend fun fetchProgress(migrationId: Int): Result<List<MigrationProgressDto>, NetworkError> {
        Logger.i("Fetching migration progress for migration $migrationId")
        return runSuspendCatching {
            client.get("/migrationProgress") {
                url { parameters.append("migrationId", migrationId.toString()) }
            }.body<List<MigrationProgressDto>>()
        }.mapError {
            Logger.e(it) { "Failed to fetch migration progress" }
            NetworkError.UnknownError
        }
    }
}