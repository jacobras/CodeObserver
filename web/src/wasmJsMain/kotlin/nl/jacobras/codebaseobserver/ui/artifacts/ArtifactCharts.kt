package nl.jacobras.codebaseobserver.ui.artifacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.contentswitcher.ContentSwitcher
import io.github.z4kn4fein.semver.toVersion
import nl.jacobras.codebaseobserver.dto.ArtifactSizeDto
import nl.jacobras.codebaseobserver.ui.chart.VersionChart

@Composable
internal fun ArtifactCharts(
    artifactSizes: List<ArtifactSizeDto>
) {
    val artifacts = artifactSizes.map { it.name }.distinct()
    var selectedArtifact by remember { mutableStateOf(artifacts.firstOrNull() ?: "") }

    if (artifacts.size > 1) {
        ContentSwitcher(
            options = artifacts,
            selectedOption = selectedArtifact,
            onOptionSelected = { selected -> selectedArtifact = selected }
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
            versionField = { it.semVer },
            metricField = { it.size },
            color = Color(0xFFE9C46A),
            modifier = Modifier
                .weight(1f)
                .height(240.dp)
        )
    }
}