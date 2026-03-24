package nl.jacobras.codebaseobserver.projects

import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.jacobras.codebaseobserver.util.data.RequestState
import nl.jacobras.codebaseobserver.dto.ProjectDto

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
internal class ProjectRepository(
    private val dataSource: ProjectDataSource
) {
    val projects = MutableStateFlow(emptyList<ProjectDto>())

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
            }
            .onErr { error ->
                loadingState.update { RequestState.Error(error) }
            }
    }

    suspend fun save(project: ProjectDto) {
        savingState.update { RequestState.Working }
        dataSource.save(project)
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
                refresh()
            }
            .onErr { error ->
                deletingState.update { it + mapOf(projectId to RequestState.Error(error)) }
            }
    }
}