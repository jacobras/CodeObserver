package nl.jacobras.codebaseobserver.dashboard.modulegraph

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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.WebElementView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.contentswitcher.ContentSwitcher
import kotlinx.browser.document
import nl.jacobras.codebaseobserver.di.RepositoryLocator
import nl.jacobras.codebaseobserver.dto.GraphModuleDto
import nl.jacobras.codebaseobserver.dto.GraphModulesDto
import nl.jacobras.codebaseobserver.dto.ModuleSortOrder
import nl.jacobras.codebaseobserver.util.data.RequestState
import nl.jacobras.codebaseobserver.util.ui.UiState
import nl.jacobras.codebaseobserver.util.ui.carbon.IntSelector
import nl.jacobras.codebaseobserver.util.ui.loading.ProgressIndicator
import org.w3c.dom.HTMLIFrameElement

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun ModuleGraph() {
    val viewModel = viewModel {
        ModuleGraphViewModel(
            modulesRepository = RepositoryLocator.modulesRepository,
            projectRepository = RepositoryLocator.projectRepository
        )
    }
    val projectId by viewModel.projectId.collectAsState()
    val graphModules by viewModel.graphModules.collectAsState(GraphModulesDto())
    val state by viewModel.uiState.collectAsState(UiState())
    val sortOrder by viewModel.sortOrder.collectAsState()

    val startModule by viewModel.startModule.collectAsState("")
    val groupingThreshold by viewModel.groupingThreshold.collectAsState()
    val layerDepth by viewModel.layerDepth.collectAsState()
    val graph by viewModel.graph.collectAsState("")
    val mermaidContainerBuilder = remember { MermaidContainerBuilder() }

    Column(modifier = Modifier) {

        when (val loading = state.loading) {
            is RequestState.Working -> {
                ProgressIndicator(modifier = Modifier.fillMaxWidth(), loading = true)
                return@Column
            }
            is RequestState.Error -> {
                ProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    error = loading.type.name,
                    onRetry = { viewModel.refresh() }
                )
                return@Column
            }
            RequestState.Idle -> Unit
        }

        if (graphModules.longestPath.isNotEmpty()) {
            BasicText(
                text = "Longest path",
                style = Carbon.typography.headingCompact02.copy(color = Carbon.theme.textPrimary),
            )
            Spacer(Modifier.height(8.dp))
            SelectionContainer {
                BasicText(
                    text = graphModules.longestPath.joinToString(separator = " » "),
                    style = Carbon.typography.code01.copy(color = Carbon.theme.textSecondary),
                )
            }
            Spacer(Modifier.height(32.dp))
        }

        Row {
            ModuleList(
                modules = graphModules.modules,
                startModule = startModule,
                onSelectModule = { viewModel.startModule.value = it },
                groupingThreshold = groupingThreshold,
                onGroupingThresholdChange = { viewModel.groupingThreshold.value = it },
                layerDepth = layerDepth,
                onLayerDepthChange = { viewModel.layerDepth.value = it },
                sortOrder = sortOrder,
                onSortOrderChange = { viewModel.setSortOrder(it) },
                modifier = Modifier.width(350.dp).padding(end = 16.dp)
            )

            if (projectId == null) {
                BasicText(
                    text = "Select a project to see the module graph",
                    style = Carbon.typography.body02,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val graphSrc = mermaidContainerBuilder.buildMermaidWebPage(mermaidGraph = graph)
                WebElementView(
                    factory = {
                        (document.createElement("iframe") as HTMLIFrameElement)
                            .apply {
                                srcdoc = graphSrc
                                frameBorder = "0"
                            }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { iframe -> iframe.srcdoc = graphSrc }
                )
            }
        }
    }
}

@Suppress("MagicNumber")
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
                )
            ) { module ->
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