package nl.jacobras.codebaseobserver.ui.modulegraph

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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.WebElementView
import com.gabrieldrn.carbon.Carbon
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.browser.document
import nl.jacobras.codebaseobserver.ui.carbon.IntSelector
import org.w3c.dom.HTMLIFrameElement

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DependencyGraph(
    projectId: String,
    client: HttpClient,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier)
    {
        var startModule by remember { mutableStateOf("") }
        var groupingThreshold by remember { mutableStateOf(3) }
        var layerDepth by remember { mutableStateOf(30) }
        var modules by remember { mutableStateOf<List<String>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(projectId) {
            try {
                modules = client.get("/modules") {
                    url { parameters.append("projectId", projectId) }
                }.body()
                isLoading = false
            } catch (e: Throwable) {
                isLoading = false
                modules = emptyList()
            }
        }

        Row {
            ModuleList(
                modules = modules,
                selectedModule = startModule,
                onSelectModule = { startModule = it },
                groupingThreshold = groupingThreshold,
                onGroupingThresholdChange = { groupingThreshold = it },
                layerDepth = layerDepth,
                onLayerDepthChange = { layerDepth = it },
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
    modules: List<String>,
    selectedModule: String,
    onSelectModule: (String) -> Unit,
    groupingThreshold: Int,
    onGroupingThresholdChange: (Int) -> Unit,
    layerDepth: Int,
    onLayerDepthChange: (Int) -> Unit,
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
                onValueChanged = onLayerDepthChange,
                values = listOf(1, 2, 3, 5, 10, 20, 30)
            )
        }
        Spacer(Modifier.height(32.dp))

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
                        fontWeight = if (selectedModule == "") FontWeight.Bold else FontWeight.Normal
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

            items(modules) { module ->
                BasicText(
                    text = module,
                    style = Carbon.typography.body02.copy(
                        fontWeight = if (module == selectedModule) FontWeight.Bold else FontWeight.Normal
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectModule(module) }
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}