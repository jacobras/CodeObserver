package nl.jacobras.codebaseobserver.modulegraph

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.WebElementView
import co.touchlab.kermit.Logger
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.contentswitcher.ContentSwitcher
import com.gabrieldrn.carbon.loading.SmallLoading
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.browser.document
import nl.jacobras.codebaseobserver.dto.GraphModuleDto
import nl.jacobras.codebaseobserver.dto.ModuleSortOrder
import nl.jacobras.codebaseobserver.ui.carbon.IntSelector
import org.w3c.dom.HTMLIFrameElement

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun DependencyGraph(
    client: HttpClient,
    projectId: String
) {
    Column(modifier = Modifier)
    {
        var startModule by remember { mutableStateOf("") }
        var groupingThreshold by remember { mutableStateOf(3) }
        var layerDepth by remember { mutableStateOf(30) }
        var sortOrder by remember { mutableStateOf(ModuleSortOrder.Alphabetical) }
        var modules by remember { mutableStateOf<List<GraphModuleDto>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(projectId, sortOrder) {
            isLoading = true

            try {
                modules = client.get("/modules") {
                    url {
                        parameters.append("projectId", projectId)
                        parameters.append("sort", sortOrder.id)
                    }
                }.body()
                isLoading = false
            } catch (e: Throwable) {
                Logger.e(e) { "Failed to load modules" }
                isLoading = false
                modules = emptyList()
            }
        }

        if (isLoading) {
            SmallLoading()
        }

        Row {
            ModuleList(
                modules = modules,
                startModule = startModule,
                onSelectModule = { startModule = it },
                groupingThreshold = groupingThreshold,
                onGroupingThresholdChange = { groupingThreshold = it },
                layerDepth = layerDepth,
                onLayerDepthChange = { layerDepth = it },
                sortOrder = sortOrder,
                onSortOrderChange = { sortOrder = it },
                modifier = Modifier.width(350.dp).padding(end = 16.dp)
            )

            val graphSrc = buildString {
                append("graph.html?projectId=")
                append(projectId)
                append("&startModule=")
                append(startModule)
                append("&groupingThreshold=")
                append(groupingThreshold)
                append("&layerDepth=")
                append(layerDepth)
            }
            WebElementView(
                factory = {
                    (document.createElement("iframe") as HTMLIFrameElement)
                        .apply {
                            src = graphSrc
                            frameBorder = "0"
                        }
                },
                modifier = Modifier.fillMaxSize(),
                update = { iframe -> iframe.src = graphSrc }
            )
        }
    }
}

@Composable
private fun ModuleList(
    modules: List<GraphModuleDto>,
    startModule: String,
    onSelectModule: (String) -> Unit,
    groupingThreshold: Int,
    onGroupingThresholdChange: (Int) -> Unit,
    layerDepth: Int,
    onLayerDepthChange: (Int) -> Unit,
    sortOrder: ModuleSortOrder,
    onSortOrderChange: (ModuleSortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IntSelector(
                label = "Grouping threshold",
                value = groupingThreshold,
                onValueChanged = onGroupingThresholdChange,
                values = listOf(2, 3, 5, 10, 20, 30)
            )
            Spacer(Modifier.width(16.dp))
            IntSelector(
                label = "Layer depth",
                value = layerDepth,
                enabled = startModule.isNotEmpty(),
                onValueChanged = onLayerDepthChange,
                values = listOf(1, 2, 3, 5, 10, 20, 30)
            )
        }
        Spacer(Modifier.height(32.dp))

        ContentSwitcher(
            options = ModuleSortOrder.entries.map { it.displayName },
            onOptionSelected = { onSortOrderChange(ModuleSortOrder.fromDisplayName(it)) },
            selectedOption = sortOrder.displayName,
        )
        Spacer(Modifier.height(16.dp))

        BasicText(
            text = "Start module",
            style = Carbon.typography.heading03
        )
        Spacer(Modifier.height(8.dp))

        LazyColumn {
            item {
                BasicText(
                    text = "None",
                    style = Carbon.typography.body02.copy(
                        fontWeight = if (startModule == "") FontWeight.Bold else FontWeight.Normal
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectModule("") }
                        .padding(vertical = 4.dp)
                )
                Spacer(Modifier.height(4.dp))
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Carbon.theme.borderSubtle00)
                )
                Spacer(Modifier.height(4.dp))
            }

            items(
                modules.sortedWith(
                    compareByDescending<GraphModuleDto> { it.score }.thenBy { it.name }
                )) { module ->
                ModuleRow(
                    module = module,
                    selected = module.name == startModule,
                    onClick = { onSelectModule(module.name) }
                )
            }
        }
    }
}

@Composable
private fun ModuleRow(
    module: GraphModuleDto,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicText(
            text = module.name,
            style = Carbon.typography.body02.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            ),
            modifier = Modifier.weight(1f)
        )
        if (module.score > 0) {
            BasicText(
                text = module.score.toString(),
                style = Carbon.typography.body01.copy(
                    fontWeight = FontWeight.Normal,
                    color = Carbon.theme.textHelper
                )
            )
        }
    }
}