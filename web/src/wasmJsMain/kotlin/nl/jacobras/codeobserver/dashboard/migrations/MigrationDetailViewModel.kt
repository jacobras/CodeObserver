package nl.jacobras.codeobserver.dashboard.migrations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrieldrn.carbon.notification.NotificationStatus
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import nl.jacobras.codeobserver.dto.MigrationId
import nl.jacobras.codeobserver.dto.MigrationProgressDto
import nl.jacobras.codeobserver.util.ui.UiState
import nl.jacobras.codeobserver.util.ui.notification.Notifier

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
            .onErr {
                Notifier.show(
                    title = "Error loading migration progress",
                    message = "Due to $it",
                    status = NotificationStatus.Error
                )
            }
    }

    fun refresh() = viewModelScope.launch {
        loadData()
    }
}