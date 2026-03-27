package nl.jacobras.codebaseobserver.dashboard.modulegraph

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import nl.jacobras.codebaseobserver.dto.GraphModulesDto
import nl.jacobras.codebaseobserver.dto.ModuleSortOrder
import nl.jacobras.codebaseobserver.util.data.NetworkError

internal class ModuleGraphDataSource(
    private val client: HttpClient
) {
    suspend fun fetchGraphModules(
        projectId: String,
        sortOrder: ModuleSortOrder
    ): Result<GraphModulesDto, NetworkError> {
        Logger.i("Fetching modules for project $projectId")
        return runSuspendCatching {
            client.get("/modules") {
                url {
                    parameters.append("projectId", projectId)
                    parameters.append("sort", sortOrder.id)
                }
            }.body<GraphModulesDto>()
        }.mapError {
            Logger.e(it) { "Failed to fetch modules" }
            NetworkError.UnknownError
        }
    }
}