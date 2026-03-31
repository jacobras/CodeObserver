package nl.jacobras.codeobserver.projects

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.jacobras.codeobserver.dto.ProjectDto
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.codeobserver.util.data.NetworkError
import nl.jacobras.codeobserver.util.data.RequestState

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
internal class ProjectRepository(
    private val dataSource: ProjectDataSource
) {
    val projects = MutableStateFlow(emptyList<ProjectDto>())

    /**
     * Selected project id, which changes the loaded data for everything in the app.
     */
    val selectedProjectId = MutableStateFlow<ProjectId?>(null)

    val loadingState = MutableStateFlow<RequestState>(RequestState.Idle)
    val savingState = MutableStateFlow<RequestState>(RequestState.Idle)
    val deletingState = MutableStateFlow<Map<ProjectId, RequestState>>(emptyMap())

    init {
        GlobalScope.launch {
            refresh()
        }
    }

    suspend fun refresh() {
        loadingState.update { RequestState.Working }
        dataSource.fetch()
            .onOk { newValue ->
                projects.value = newValue
                loadingState.update { RequestState.Idle }

                if (selectedProjectId.value == null && newValue.isNotEmpty()) {
                    Logger.i { "Auto-selecting new project: ${newValue.first().id}" }
                    selectedProjectId.value = newValue.first().id
                } else if (selectedProjectId.value != null && newValue.isEmpty()) {
                    Logger.i { "No more projects, de-selecting" }
                    selectedProjectId.value = null
                } else if (selectedProjectId.value != null && selectedProjectId.value !in newValue.map { it.id }) {
                    Logger.i { "Selected project not available anymore, auto-selecting first project" }
                    selectedProjectId.value = newValue.first().id
                }
            }
            .onErr { error ->
                loadingState.update { RequestState.Error(error) }
            }
    }

    suspend fun save(project: ProjectDto): Result<Unit, NetworkError> {
        savingState.update { RequestState.Working }
        return dataSource.save(project)
            .onOk { savingState.update { RequestState.Idle } }
            .onErr { error ->
                savingState.update { RequestState.Error(error) }
            }
    }

    suspend fun delete(projectId: ProjectId) {
        deletingState.update { it + mapOf(projectId to RequestState.Working) }
        dataSource.delete(projectId)
            .onOk {
                deletingState.update { it - projectId }
                Logger.i { "Project $projectId deleted" }
                refresh()
            }
            .onErr { error ->
                deletingState.update { it + mapOf(projectId to RequestState.Error(error)) }
            }
    }

    fun setSelectedProjectId(projectId: ProjectId) {
        Logger.i { "Switching to project: $projectId" }
        selectedProjectId.value = projectId
    }
}