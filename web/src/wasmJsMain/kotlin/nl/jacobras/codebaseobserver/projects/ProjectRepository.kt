package nl.jacobras.codebaseobserver.projects

import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.jacobras.codebaseobserver.data.RequestState
import nl.jacobras.codebaseobserver.data.UiState
import nl.jacobras.codebaseobserver.dto.ProjectDto

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
internal class ProjectRepository(
    private val dataSource: ProjectDataSource
) {
    val projects = MutableStateFlow(emptyList<ProjectDto>())
    val state = MutableStateFlow(UiState<String>())

    init {
        GlobalScope.launch {
            refresh()
        }
    }

    suspend fun refresh() {
        state.update { it.copy(loading = RequestState.Working) }
        dataSource.fetch()
            .onOk { newValue ->
                projects.value = newValue
                state.update { it.copy(loading = RequestState.Idle) }
            }
            .onErr { error ->
                state.update { it.copy(loading = RequestState.Error(error)) }
            }
    }

    suspend fun save(project: ProjectDto) {
        state.update { it.copy(saving = RequestState.Working) }
        dataSource.save(project)
            .onOk { state.update { it.copy(saving = RequestState.Idle) } }
            .onErr { error ->
                state.update { it.copy(saving = RequestState.Error(error)) }
            }
    }

    suspend fun delete(projectId: String) {
        state.update { it.copy(deleting = it.deleting + mapOf(projectId to RequestState.Working)) }
        dataSource.delete(projectId)
            .onOk {
                state.update { it.copy(deleting = it.deleting.minus(projectId)) }
                refresh()
            }
            .onErr { error ->
                state.update { it.copy(deleting = it.deleting + mapOf(projectId to RequestState.Error(error))) }
            }
    }
}