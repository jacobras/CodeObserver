package nl.jacobras.codebaseobserver.dashboard.migrations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import nl.jacobras.codebaseobserver.dto.MigrationProgressDto
import nl.jacobras.codebaseobserver.util.ui.UiState

internal class MigrationDetailViewModel(
    private val migrationProgressRepository: MigrationProgressRepository
) : ViewModel() {

    private val migrationId = MutableStateFlow(0)
    val uiState = migrationProgressRepository.loadingState.map { UiState<Nothing>(loading = it) }
    val progress = MutableStateFlow(emptyList<MigrationProgressDto>())

    init {
        viewModelScope.launch {
            migrationId.collectLatest { id ->
                if (id > 0) {
                    loadData()
                }
            }
        }
    }

    fun setMigrationId(id: Int) {
        migrationId.value = id
    }

    private suspend fun loadData() {
        migrationProgressRepository.fetchProgress(migrationId.value)
            .onOk { progress.value = it }
    }

    fun refresh() = viewModelScope.launch {
        loadData()
    }
}