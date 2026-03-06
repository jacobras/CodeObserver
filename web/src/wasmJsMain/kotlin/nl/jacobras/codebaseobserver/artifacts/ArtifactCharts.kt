package nl.jacobras.codebaseobserver.artifacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.tab.TabItem
import com.gabrieldrn.carbon.tab.TabList
import io.github.z4kn4fein.semver.toVersion
import nl.jacobras.codebaseobserver.dto.ArtifactSizeDto
import nl.jacobras.codebaseobserver.ui.chart.ChartColor
import nl.jacobras.codebaseobserver.ui.chart.VersionChart

@Composable
internal fun ArtifactCharts(
    artifactSizes: List<ArtifactSizeDto>
) {
    if (artifactSizes.isEmpty()) {
        BasicText(
            modifier = Modifier.fillMaxWidth(),
            text = "No artifacts found",
            style = Carbon.typography.body02
        )
        return
    }

    val artifacts = artifactSizes.map { it.name }.distinct()
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

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        VersionChart(
            title = "Artifact size",
            records = artifactSizes
                .filter { it.name == selectedArtifact }
                .sortedWith { a, b ->
                    val a = a.semVer.toVersion()
                    val b = b.semVer.toVersion()
                    a.compareTo(b)
                },
            versionField = { it.semVer.toVersion() },
            metricField = { it.size },
            color = ChartColor.Goldenrod,
            modifier = Modifier
                .weight(1f)
                .height(240.dp)
        )
    }
}