package nl.jacobras.codebaseobserver.dashboard.migrations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import nl.jacobras.codebaseobserver.dto.MigrationDto
import nl.jacobras.codebaseobserver.dto.MigrationId
import nl.jacobras.codebaseobserver.projects.ProjectRepository
import nl.jacobras.codebaseobserver.util.ui.UiState

internal class MigrationsViewModel(
    private val migrationsRepository: MigrationsRepository,
    projectRepository: ProjectRepository
) : ViewModel() {

    private val projectId = projectRepository.selectedProjectId
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
    }

    fun refresh() = viewModelScope.launch {
        loadData()
    }

    fun save(id: MigrationId?, name: String, description: String, type: String, rule: String) = viewModelScope.launch {
        val projectId = projectId.value ?: return@launch
        migrationsRepository.save(id, projectId, name, description, type, rule)
            .onOk { refresh() }
    }

    fun delete(id: MigrationId) = viewModelScope.launch {
        migrationsRepository.delete(id)
            .onOk { refresh() }
    }
}