package nl.jacobras.codebaseobserver.artifacts

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.tab.TabItem
import com.gabrieldrn.carbon.tab.TabList
import io.github.z4kn4fein.semver.toVersion
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import nl.jacobras.codebaseobserver.dto.ArtifactSizeDto
import nl.jacobras.codebaseobserver.ui.chart.ChartColor
import nl.jacobras.codebaseobserver.ui.chart.VersionChart
import nl.jacobras.codebaseobserver.ui.table.DataTable
import nl.jacobras.humanreadable.HumanReadable

@Composable
internal fun ArtifactCharts(
    client: HttpClient,
    projectId: String
) {
    val artifactSizes by produceState(emptyList<ArtifactSizeDto>(), projectId) {
        value = client.get("/artifactSizes") {
            url { parameters.append("projectId", projectId) }
        }.body()
    }

    if (artifactSizes.isEmpty()) {
        BasicText(
            modifier = Modifier.fillMaxWidth(),
            text = "No artifacts found",
            style = Carbon.typography.body02
        )
        return
    }

    val artifacts = artifactSizes.map { it.name }.distinct().sortedBy { it }
    var selectedArtifact by remember { mutableStateOf(artifacts.first()) }

    if (artifacts.size > 1) {
        val tabs = artifacts.map { TabItem(label = it) }

        TabList(
            tabs = tabs,
            selectedTab = tabs.first { it.label == selectedArtifact },
            onTabSelected = { tab ->
                selectedArtifact = artifacts.first { it == tab.label }
            }
        )
        Spacer(Modifier.height(16.dp))
    }

    ArtifactDetail(
        allArtifactSizes = artifactSizes,
        artifactName = selectedArtifact
    )
}

@Composable
private fun ArtifactDetail(
    allArtifactSizes: List<ArtifactSizeDto>,
    artifactName: String
) {
    val artifactSizesOldestFirst = allArtifactSizes
        .filter { it.name == artifactName }
        .sortedWith { a, b ->
            val a = a.semVer.toVersion()
            val b = b.semVer.toVersion()
            a.compareTo(b)
        }
    val artifactSizesNewestFirst = artifactSizesOldestFirst.reversed()

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        VersionChart(
            title = "Artifact size",
            records = artifactSizesOldestFirst,
            versionField = { it.semVer.toVersion() },
            metricField = { it.size },
            color = ChartColor.Goldenrod,
            modifier = Modifier
                .weight(1f)
                .height(240.dp)
        )
        DataTable(
            modifier = Modifier.weight(1f),
            columnHeadings = listOf("Artifact", "Version", "Size"),
            rowCount = artifactSizesNewestFirst.size,
            cellContent = { rowIndex, columnIndex, modifier ->
                val item = artifactSizesNewestFirst[rowIndex]
                when (columnIndex) {
                    0 -> SelectionContainer(modifier) {
                        BasicText(
                            text = item.name,
                            style = Carbon.typography.body02,
                            modifier = modifier
                        )
                    }
                    1 -> SelectionContainer(modifier) {
                        BasicText(
                            text = item.semVer,
                            style = Carbon.typography.code02,
                        )
                    }
                    2 -> SelectionContainer(modifier) {
                        BasicText(
                            text = HumanReadable.fileSize(item.size, decimals = 1),
                            style = Carbon.typography.body02,
                        )
                    }
                }
            }
        )
    }
}