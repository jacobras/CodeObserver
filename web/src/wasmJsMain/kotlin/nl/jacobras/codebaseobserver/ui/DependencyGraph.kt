package nl.jacobras.codebaseobserver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.gabrieldrn.carbon.checkbox.Checkbox
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.browser.document
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
        var alwaysGroup by remember { mutableStateOf(false) }
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
                alwaysGroup = alwaysGroup,
                onAlwaysGroupChange = { alwaysGroup = it },
                modifier = Modifier.width(300.dp)
            )

            val graphSrc = buildString {
                append("graph.html?projectId=")
                append(projectId)
                append("&startModule=")
                append(startModule)
                append("&groupThreshold=3")
                append("&alwaysGroup=")
                append(alwaysGroup)
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
    alwaysGroup: Boolean,
    onAlwaysGroupChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Checkbox(
            label = "Always group",
            checked = alwaysGroup,
            onClick = { onAlwaysGroupChange(!alwaysGroup) }
        )
        Spacer(Modifier.height(16.dp))

        BasicText(
            text = "Start module",
            style = Carbon.typography.heading03
        )

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