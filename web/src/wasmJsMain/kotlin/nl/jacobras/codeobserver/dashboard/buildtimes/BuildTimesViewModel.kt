package nl.jacobras.codeobserver.dashboard.buildtimes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrieldrn.carbon.notification.NotificationStatus
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import nl.jacobras.codeobserver.dto.BuildTimeDto
import nl.jacobras.codeobserver.projects.ProjectRepository
import nl.jacobras.codeobserver.util.ui.UiState
import nl.jacobras.codeobserver.util.ui.notification.Notifier

internal class BuildTimesViewModel(
    private val buildTimesRepository: BuildTimesRepository,
    projectRepository: ProjectRepository
) : ViewModel() {

    val projectId = projectRepository.selectedProjectId
    val uiState = buildTimesRepository.loadingState.map { UiState<Nothing>(loading = it) }
    val buildTimes = MutableStateFlow(emptyList<BuildTimeDto>())

    init {
        viewModelScope.launch {
            projectId
                .filterNotNull()
                .distinctUntilChanged()
                .collectLatest { refresh() }
        }
    }

    private suspend fun loadData() {
        val projectId = projectId.value ?: return
        buildTimesRepository.fetchBuildTimes(projectId)
            .onOk { buildTimes.value = it }
            .onErr {
                Notifier.show(
                    title = "Error loading build times",
                    message = "Due to $it",
                    status = NotificationStatus.Error
                )
            }
    }

    fun refresh() = viewModelScope.launch {
        loadData()
    }
}