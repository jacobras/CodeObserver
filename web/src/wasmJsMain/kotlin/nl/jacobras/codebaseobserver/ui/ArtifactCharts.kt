package nl.jacobras.codebaseobserver.ui

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
                    val aParts = a.semVer.split(".").map { it.toIntOrNull() ?: 0 }
                    val bParts = b.semVer.split(".").map { it.toIntOrNull() ?: 0 }

                    for (i in 0..2) {
                        val cmp = aParts.getOrElse(i) { 0 }.compareTo(bParts.getOrElse(i) { 0 })
                        if (cmp != 0) return@sortedWith cmp
                    }
                    0
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