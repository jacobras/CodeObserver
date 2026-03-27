package nl.jacobras.codebaseobserver.dashboard.migrations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.tab.TabItem
import com.gabrieldrn.carbon.tab.TabList
import nl.jacobras.codebaseobserver.di.RepositoryLocator
import nl.jacobras.codebaseobserver.dto.MigrationDto
import nl.jacobras.codebaseobserver.util.data.RequestState
import nl.jacobras.codebaseobserver.util.ui.UiState
import nl.jacobras.codebaseobserver.util.ui.chart.ChartColor
import nl.jacobras.codebaseobserver.util.ui.chart.TimeChart
import nl.jacobras.codebaseobserver.util.ui.chart.TimeView
import nl.jacobras.codebaseobserver.util.ui.chart.TimeViewSelector
import nl.jacobras.codebaseobserver.util.ui.loading.ProgressIndicator
import nl.jacobras.codebaseobserver.util.ui.table.DataTable
import nl.jacobras.codebaseobserver.util.ui.text.excerpt

@Composable
internal fun Migrations(
    timeView: TimeView,
    onSelectTimeView: (TimeView) -> Unit
) {
    val viewModel = viewModel {
        MigrationsViewModel(
            migrationsRepository = RepositoryLocator.migrationsRepository,
            projectRepository = RepositoryLocator.projectRepository
        )
    }
    val migrations by viewModel.migrations.collectAsState(emptyList())
    val state by viewModel.uiState.collectAsState(UiState())

    val isLoading = state.loading is RequestState.Working
    val loadingError = (state.loading as? RequestState.Error)?.type?.name ?: ""
    val updateError = (state.saving as? RequestState.Error)?.type?.name
        ?: state.deleting.values.filterIsInstance<RequestState.Error>().firstOrNull()?.type?.name
        ?: ""

    if (isLoading || loadingError.isNotEmpty() || updateError.isNotEmpty()) {
        ProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            loading = isLoading,
            error = updateError.ifEmpty { loadingError },
            onRetry = if (loadingError.isNotEmpty()) {
                { viewModel.refresh() }
            } else {
                null
            }
        )
        return
    }

    Column(Modifier.fillMaxWidth()) {
        val overviewTab = TabItem("Overview")
        val tabs = listOf(overviewTab) + migrations
            .sortedBy { it.name }
            .map { TabItem(label = it.name) }
        var selectedTab by remember { mutableStateOf(overviewTab) }

        TabList(
            tabs = tabs,
            selectedTab = selectedTab,
            onTabSelected = { tab ->
                selectedTab = tab
            }
        )
        Spacer(Modifier.height(16.dp))

        if (selectedTab == overviewTab) {
            MigrationsOverview(
                migrations = migrations,
                onSave = { id, name, description, type, rule ->
                    viewModel.save(id, name, description, type, rule)
                },
                onDelete = { viewModel.delete(it) }
            )
        } else {
            val selectedMigration = migrations.first { it.name == selectedTab.label }
            MigrationDetail(
                migration = selectedMigration,
                timeView = timeView,
                onSelectTimeView = onSelectTimeView
            )
        }
    }
}

@Composable
private fun MigrationDetail(
    migration: MigrationDto,
    timeView: TimeView,
    onSelectTimeView: (TimeView) -> Unit
) {
    val viewModel = viewModel {
        MigrationDetailViewModel(
            migrationProgressRepository = RepositoryLocator.migrationProgressRepository
        )
    }
    val progress by viewModel.progress.collectAsState(emptyList())
    val state by viewModel.uiState.collectAsState(UiState())

    LaunchedEffect(migration.id) {
        viewModel.setMigrationId(migration.id)
    }

    BasicText(
        modifier = Modifier.fillMaxWidth(),
        text = buildAnnotatedString {
            withStyle(Carbon.typography.body02.toSpanStyle()) {
                append(migration.type)
                append(": ")
            }
            withStyle(Carbon.typography.code02.toSpanStyle()) {
                append(migration.rule)
            }
        }
    )
    if (migration.description.isNotBlank()) {
        Spacer(Modifier.height(8.dp))
        BasicText(
            modifier = Modifier.fillMaxWidth(),
            text = migration.description,
            style = Carbon.typography.body01
        )
    }
    Spacer(Modifier.height(32.dp))

    when (val loading = state.loading) {
        is RequestState.Working -> {
            ProgressIndicator(modifier = Modifier.fillMaxWidth(), loading = true)
            return
        }
        is RequestState.Error -> {
            ProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                error = loading.type.name,
                onRetry = { viewModel.refresh() }
            )
            return
        }
        RequestState.Idle -> Unit
    }

    val progressOldestFirst = progress.sortedBy { it.gitDate }
    val progressNewestFirst = progressOldestFirst.reversed()

    if (progress.isEmpty()) {
        BasicText(
            modifier = Modifier.fillMaxWidth(),
            text = "No progress data found",
            style = Carbon.typography.body02
        )
        return
    }

    TimeViewSelector(
        selected = timeView,
        onSelect = { onSelectTimeView(it) }
    )
    Spacer(Modifier.height(16.dp))

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        TimeChart(
            modifier = Modifier.weight(1f),
            title = "Usages",
            records = progressOldestFirst,
            dateField = { it.gitDate },
            metricField = { it.count },
            timeView = timeView,
            color = ChartColor.RoseDust
        )
        DataTable(
            modifier = Modifier.weight(1f),
            columnHeadings = listOf("Git hash", "Date", "Count"),
            rowCount = progressNewestFirst.size,
            cellContent = { rowIndex, columnIndex, modifier ->
                val item = progressNewestFirst[rowIndex]
                when (columnIndex) {
                    0 -> SelectionContainer(modifier) {
                        BasicText(
                            text = item.gitHash.excerpt(),
                            style = Carbon.typography.code01
                        )
                    }
                    1 -> SelectionContainer(modifier) {
                        BasicText(
                            text = item.gitDate.toString(),
                            style = Carbon.typography.bodyCompact01
                        )
                    }
                    2 -> SelectionContainer(modifier) {
                        BasicText(
                            text = item.count.toString(),
                            style = Carbon.typography.bodyCompact01
                        )
                    }
                }
            }
        )
    }
}