package nl.jacobras.codebaseobserver.projects

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
import nl.jacobras.codebaseobserver.dto.ProjectDto
import nl.jacobras.codebaseobserver.util.data.NetworkError
import nl.jacobras.codebaseobserver.util.data.RequestState

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
internal class ProjectRepository(
    private val dataSource: ProjectDataSource
) {
    val projects = MutableStateFlow(emptyList<ProjectDto>())

    /**
     * Selected project id, which changes the loaded data for everything in the app.
     */
    val selectedProjectId = MutableStateFlow("")

    val loadingState = MutableStateFlow<RequestState>(RequestState.Idle)
    val savingState = MutableStateFlow<RequestState>(RequestState.Idle)
    val deletingState = MutableStateFlow<Map<String, RequestState>>(emptyMap())

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

                // Auto-select or auto-de-select the first project available.
                if (selectedProjectId.value.isEmpty() && newValue.isNotEmpty()) {
                    selectedProjectId.value = newValue.first().id
                } else if (selectedProjectId.value.isNotEmpty() && newValue.isEmpty()) {
                    selectedProjectId.value = ""
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

    suspend fun delete(projectId: String) {
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

    fun setSelectedProjectId(projectId: String) {
        Logger.i { "Switching to project: $projectId" }
        selectedProjectId.value = projectId
    }
}