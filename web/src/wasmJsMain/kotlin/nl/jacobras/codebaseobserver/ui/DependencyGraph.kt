package nl.jacobras.codebaseobserver.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.WebElementView
import com.gabrieldrn.carbon.dropdown.Dropdown
import com.gabrieldrn.carbon.dropdown.base.DropdownInteractiveState
import com.gabrieldrn.carbon.dropdown.base.DropdownOption
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

        val moduleOptions = mapOf("" to DropdownOption("None")) +
                modules.associateWith { DropdownOption(it) }
        Row {
            Dropdown(
                label = "Focus module",
                placeholder = "Select a module",
                options = moduleOptions,
                selectedOption = startModule,
                onOptionSelected = { startModule = it },
                isInlined = true,
                state = if (!isLoading) DropdownInteractiveState.Enabled else DropdownInteractiveState.Disabled
            )
            Spacer(Modifier.width(16.dp))

            val graphSrc = buildString {
                append("graph.html?projectId=")
                append(projectId)
                append("&startModule=")
                append(startModule)
                append("&groupThreshold=30")
            }
            WebElementView(
                factory = {
                    (document.createElement("iframe") as HTMLIFrameElement)
                        .apply {
                            src = graphSrc
                            frameBorder = "0"
                        }
                },
                modifier = Modifier
                    .requiredSizeIn(maxWidth = 1800.dp)
                    .aspectRatio(2f),
                update = { iframe -> iframe.src = graphSrc }
            )
        }
    }
}