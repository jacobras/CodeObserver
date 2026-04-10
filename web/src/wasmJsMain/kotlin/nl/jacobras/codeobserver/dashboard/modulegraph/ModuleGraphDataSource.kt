package nl.jacobras.codeobserver.dashboard.modulegraph

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.recoverIf
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import nl.jacobras.codeobserver.dto.GraphModulesDto
import nl.jacobras.codeobserver.dto.GraphVisualInfoDto
import nl.jacobras.codeobserver.dto.ModuleSortOrder
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.codeobserver.util.data.NetworkError

internal interface ModuleGraphDataSource {
    suspend fun fetchGraphModules(
        projectId: ProjectId,
        sortOrder: ModuleSortOrder
    ): Result<GraphModulesDto, NetworkError>

    suspend fun fetchGraphInfo(
        projectId: ProjectId
    ): Result<GraphVisualInfoDto, NetworkError>
}

internal class ModuleGraphDataSourceImpl(
    private val client: HttpClient
) : ModuleGraphDataSource {
    override suspend fun fetchGraphModules(
        projectId: ProjectId,
        sortOrder: ModuleSortOrder
    ): Result<GraphModulesDto, NetworkError> {
        Logger.i("Fetching modules for project ${projectId.value}")
        return runSuspendCatching {
            client.get("/modules") {
                url {
                    parameters.append("projectId", projectId.value)
                    parameters.append("sort", sortOrder.id)
                }
            }.body<GraphModulesDto>()
        }.mapError {
            Logger.e(it) { "Failed to fetch modules" }
            NetworkError.UnknownError
        }
    }

    override suspend fun fetchGraphInfo(
        projectId: ProjectId
    ): Result<GraphVisualInfoDto, NetworkError> {
        Logger.i("Fetching graph visual info for project ${projectId.value}")
        return runSuspendCatching {
            client.get("/graphVisualInfo") {
                url {
                    parameters.append("projectId", projectId.value)
                }
            }.body<GraphVisualInfoDto>()
        }.recoverIf(
            predicate = { it is ClientRequestException && it.response.status == HttpStatusCode.NotFound },
            transform = { GraphVisualInfoDto() }
        ).mapError {
            Logger.e(it) { "Failed to fetch graph visual info" }
            NetworkError.UnknownError
        }
    }
}