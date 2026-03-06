package nl.jacobras.codebaseobserver.migrations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.tab.TabItem
import com.gabrieldrn.carbon.tab.TabList
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import nl.jacobras.codebaseobserver.dto.MigrationDto
import nl.jacobras.codebaseobserver.dto.MigrationProgressDto
import nl.jacobras.codebaseobserver.ui.chart.ChartColor
import nl.jacobras.codebaseobserver.ui.chart.TimeChart
import nl.jacobras.codebaseobserver.ui.chart.TimeView
import nl.jacobras.codebaseobserver.ui.chart.TimeViewSelector
import nl.jacobras.codebaseobserver.ui.table.DataTable

@Composable
internal fun Migrations(
    client: HttpClient,
    projectId: String
) {
    var refreshKey by remember { mutableStateOf(0) }
    val migrations by produceState(emptyList<MigrationDto>(), projectId, refreshKey) {
        value = client.get("/migrations") {
            url { parameters.append("projectId", projectId) }
        }.body()
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
                client = client,
                projectId = projectId,
                migrations = migrations,
                onRefresh = { refreshKey++ }
            )
        } else {
            val selectedMigration = migrations.first { it.name == selectedTab.label }
            MigrationDetail(client = client, migration = selectedMigration)
        }
    }
}

@Composable
private fun MigrationDetail(
    client: HttpClient,
    migration: MigrationDto
) {
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

    val progress by produceState(emptyList<MigrationProgressDto>(), migration.id) {
        value = client.get("/migrationProgress") {
            url { parameters.append("migrationId", migration.id.toString()) }
        }.body()
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

    var timeView by remember { mutableStateOf(TimeView.Last7Days) }
    TimeViewSelector(
        selected = timeView,
        onSelect = { timeView = it }
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
                            text = item.gitHash,
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