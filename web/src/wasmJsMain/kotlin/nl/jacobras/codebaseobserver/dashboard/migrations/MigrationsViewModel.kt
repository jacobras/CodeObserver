package nl.jacobras.codebaseobserver.dashboard.migrations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import nl.jacobras.codebaseobserver.dto.MigrationDto
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
                if (id.isNotEmpty()) {
                    loadData()
                }
            }
        }
    }

    private suspend fun loadData() {
        migrationsRepository.fetchMigrations(projectId.value)
            .onOk { migrations.value = it }
    }

    fun refresh() = viewModelScope.launch {
        loadData()
    }

    fun save(id: Int?, name: String, description: String, type: String, rule: String) = viewModelScope.launch {
        migrationsRepository.save(id, projectId.value, name, description, type, rule)
            .onOk { refresh() }
    }

    fun delete(id: Int) = viewModelScope.launch {
        migrationsRepository.delete(id)
            .onOk { refresh() }
    }
}