package nl.jacobras.codeobserver.dashboard.migrations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrieldrn.carbon.notification.NotificationStatus
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import nl.jacobras.codeobserver.dto.MigrationDto
import nl.jacobras.codeobserver.dto.MigrationId
import nl.jacobras.codeobserver.projects.ProjectRepository
import nl.jacobras.codeobserver.util.ui.UiState
import nl.jacobras.codeobserver.util.ui.notification.Notifier

internal class MigrationsViewModel(
    private val migrationsRepository: MigrationsRepository,
    projectRepository: ProjectRepository
) : ViewModel() {

    val projectId = projectRepository.selectedProjectId
    val uiState = combine(
        migrationsRepository.loadingState,
        migrationsRepository.savingState,
        migrationsRepository.deletingState
    ) { loading, saving, deleting ->
        UiState(loading = loading, saving = saving, deleting = deleting)
    }
    val migrations = MutableStateFlow(emptyList<MigrationDto>())

    init {
        viewModelScope.launch {
            projectId.collectLatest { id ->
                if (id != null) {
                    loadData()
                }
            }
        }
    }

    private suspend fun loadData() {
        val projectId = projectId.value ?: return
        migrationsRepository.fetchMigrations(projectId)
            .onOk { migrations.value = it }
            .onErr {
                Notifier.show(
                    title = "Error loading migrations",
                    message = "Due to $it",
                    status = NotificationStatus.Error
                )
            }
    }

    fun refresh() = viewModelScope.launch {
        loadData()
    }

    fun save(id: MigrationId?, name: String, description: String, type: String, rule: String) = viewModelScope.launch {
        val projectId = projectId.value ?: return@launch
        migrationsRepository.save(id, projectId, name, description, type, rule)
            .onOk {
                refresh()
                Notifier.show(
                    title = "Migration '$name' saved",
                    status = NotificationStatus.Success
                )
            }
            .onErr {
                Notifier.show(
                    title = "Error saving migration '$name'",
                    message = "Due to $it",
                    status = NotificationStatus.Error
                )
            }
    }

    fun delete(id: MigrationId) = viewModelScope.launch {
        migrationsRepository.delete(id)
            .onOk {
                refresh()
                Notifier.show(
                    title = "Migration deleted",
                    message = "Migration deleted",
                    status = NotificationStatus.Success
                )
            }
            .onErr {
                Notifier.show(
                    title = "Error deleting migration",
                    message = "Due to $it",
                    status = NotificationStatus.Error
                )
            }
    }
}