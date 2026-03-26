package nl.jacobras.codebaseobserver.dashboard.modulegraph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import nl.jacobras.codebaseobserver.dto.ModuleGraphSettingDto
import nl.jacobras.codebaseobserver.projects.ProjectRepository
import nl.jacobras.codebaseobserver.util.ui.UiState

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
        refresh()

        viewModelScope.launch {
            projectId.collect { refresh() }
        }
    }

    fun refresh() = viewModelScope.launch {
        moduleGraphSettingsRepository.fetchSettings(projectId.value)
            .onOk { settings.value = it }
    }

    fun save(id: Int?, type: String, data: String) = viewModelScope.launch {
        moduleGraphSettingsRepository.save(id, projectId.value, type, data)
            .onOk { refresh() }
    }

    fun delete(id: Int) = viewModelScope.launch {
        moduleGraphSettingsRepository.delete(id)
            .onOk { refresh() }
    }
}