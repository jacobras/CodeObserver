package nl.jacobras.codeobserver.dashboard.modulegraph

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import nl.jacobras.codeobserver.dto.GraphModulesDto
import nl.jacobras.codeobserver.dto.ModuleSortOrder
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.codeobserver.util.data.NetworkError

internal interface ModuleGraphDataSource {
    suspend fun fetchGraphModules(
        projectId: ProjectId,
        sortOrder: ModuleSortOrder
    ): Result<GraphModulesDto, NetworkError>

    suspend fun fetchGraph(
        projectId: ProjectId,
        startModule: String,
        groupingThreshold: Int,
        layerDepth: Int
    ): Result<String, NetworkError>
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

    override suspend fun fetchGraph(
        projectId: ProjectId,
        startModule: String,
        groupingThreshold: Int,
        layerDepth: Int
    ): Result<String, NetworkError> {
        Logger.i("Fetching module graph for project ${projectId.value}")
        return runSuspendCatching {
            client.get("/moduleGraph") {
                url {
                    parameters.append("projectId", projectId.value)
                    parameters.append("startModule", startModule)
                    parameters.append("groupingThreshold", groupingThreshold.toString())
                    parameters.append("layerDepth", layerDepth.toString())
                }
            }.body<String>()
        }.mapError {
            Logger.e(it) { "Failed to fetch module graph" }
            NetworkError.UnknownError
        }
    }
}