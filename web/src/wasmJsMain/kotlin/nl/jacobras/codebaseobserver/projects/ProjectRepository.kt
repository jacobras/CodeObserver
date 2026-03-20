package nl.jacobras.codebaseobserver.projects

import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import nl.jacobras.codebaseobserver.data.RequestState
import nl.jacobras.codebaseobserver.dto.ProjectDto

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
internal class ProjectRepository(
    private val dataSource: ProjectDataSource
) {
    val projects = MutableStateFlow(emptyList<ProjectDto>())
    val requestState = MutableStateFlow<RequestState>(RequestState.Idle)
    val modifyingState = MutableStateFlow<RequestState>(RequestState.Idle)

    init {
        GlobalScope.launch {
            refresh()
        }
    }

    suspend fun refresh() {
        requestState.value = RequestState.Working
        dataSource.fetch()
            .onOk {
                projects.value = it
                requestState.value = RequestState.Idle
            }
            .onErr { requestState.value = RequestState.Error(it) }
    }

    suspend fun save(project: ProjectDto) {
        modifyingState.value = RequestState.Working
        dataSource.save(project)
            .onOk { modifyingState.value = RequestState.Idle }
            .onErr { modifyingState.value = RequestState.Error(it) }
    }

    suspend fun delete(projectId: String) {
        modifyingState.value = RequestState.Working
        dataSource.delete(projectId)
            .onOk {
                modifyingState.value = RequestState.Idle
                refresh()
            }
            .onErr { modifyingState.value = RequestState.Error(it) }
    }
}