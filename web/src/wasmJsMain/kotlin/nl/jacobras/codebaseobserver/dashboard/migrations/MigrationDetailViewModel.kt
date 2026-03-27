package nl.jacobras.codebaseobserver.dashboard.migrations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import nl.jacobras.codebaseobserver.dto.MigrationId
import nl.jacobras.codebaseobserver.dto.MigrationProgressDto
import nl.jacobras.codebaseobserver.util.ui.UiState

internal class MigrationDetailViewModel(
    private val migrationProgressRepository: MigrationProgressRepository
) : ViewModel() {

    private val migrationId = MutableStateFlow<MigrationId?>(null)
    val uiState = migrationProgressRepository.loadingState.map { UiState<Nothing>(loading = it) }
    val progress = MutableStateFlow(emptyList<MigrationProgressDto>())

    init {
        viewModelScope.launch {
            migrationId.collectLatest { id ->
                if (id != null) {
                    loadData()
                }
            }
        }
    }

    fun setMigrationId(id: MigrationId) {
        migrationId.value = id
    }

    private suspend fun loadData() {
        val migrationId = migrationId.value ?: return
        migrationProgressRepository.fetchProgress(migrationId)
            .onOk { progress.value = it }
    }

    fun refresh() = viewModelScope.launch {
        loadData()
    }
}