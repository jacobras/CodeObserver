package nl.jacobras.codeobserver.dashboard.modulegraph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import nl.jacobras.codeobserver.dto.ModuleGraphSettingDto
import nl.jacobras.codeobserver.dto.ModuleGraphSettingId
import nl.jacobras.codeobserver.projects.ProjectRepository
import nl.jacobras.codeobserver.util.ui.UiState

internal class ModuleRulesViewModel(
    private val moduleGraphSettingsRepository: ModuleGraphSettingsRepository,
    projectRepository: ProjectRepository
) : ViewModel() {

    private val projectId = projectRepository.selectedProjectId
    val uiState = combine(
        moduleGraphSettingsRepository.loadingState,
        moduleGraphSettingsRepository.savingState,
        moduleGraphSettingsRepository.deletingState
    ) { loading, saving, deleting ->
        UiState(loading = loading, saving = saving, deleting = deleting)
    }
    val settings = MutableStateFlow(emptyList<ModuleGraphSettingDto>())

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
        moduleGraphSettingsRepository.fetchSettings(projectId)
            .onOk { settings.value = it }
    }

    fun refresh() = viewModelScope.launch {
        loadData()
    }

    fun save(id: ModuleGraphSettingId?, type: String, data: String) = viewModelScope.launch {
        val projectId = projectId.value ?: return@launch
        moduleGraphSettingsRepository.save(id, projectId, type, data)
            .onOk { refresh() }
    }

    fun delete(id: ModuleGraphSettingId) = viewModelScope.launch {
        moduleGraphSettingsRepository.delete(id)
            .onOk { refresh() }
    }
}