package nl.jacobras.codeobserver.desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.button.Button
import com.gabrieldrn.carbon.button.ButtonSize
import com.gabrieldrn.carbon.button.ButtonType
import com.gabrieldrn.carbon.foundation.color.CarbonLayer
import com.gabrieldrn.carbon.foundation.color.layerBackground
import com.gabrieldrn.carbon.textinput.TextInput
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.codeobserver.util.ui.button.SmallProgressButton
import nl.jacobras.codeobserver.util.ui.theme.COTheme

fun main() = application {
    val serverLauncher = remember { ServerLauncher() }
    val cliLauncher = remember { CliLauncher() }

    Window(
        onCloseRequest = ::exitApplication,
        title = "CodeObserver"
    ) {
        COTheme {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ServerPanel(serverLauncher)
                CliPanel(cliLauncher)
            }
        }
    }
}

@Composable
private fun ServerPanel(server: ServerLauncher) {
    val isRunning by server.started.collectAsState()

    CarbonLayer {
        Column(
            Modifier
                .layerBackground()
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            BasicText(
                text = "Server",
                style = Carbon.typography.heading03.copy(
                    Carbon.theme.textPrimary
                )
            )
            BasicText(
                text = buildAnnotatedString {
                    if (isRunning) {
                        append("Running on ")
                        withLink(
                            LinkAnnotation.Url(
                                url = "http://localhost:8080",
                                styles = TextLinkStyles(
                                    style = SpanStyle(color = Carbon.theme.linkPrimary)
                                )
                            )
                        ) {
                            append("http://localhost:8080")
                        }
                        append("! Open it to see the dashboard.")
                    } else {
                        append("Not running")
                    }
                },
                style = Carbon.typography.body01.copy(Carbon.theme.textPrimary)
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    label = "Start",
                    onClick = { server.start() },
                    isEnabled = !isRunning,
                    buttonSize = ButtonSize.Medium
                )
                Button(
                    label = "Stop",
                    onClick = { server.stop() },
                    isEnabled = isRunning,
                    buttonSize = ButtonSize.Medium
                )
            }
        }
    }
}

@Composable
private fun CliPanel(cliLauncher: CliLauncher) {
    var path by remember { mutableStateOf("") }
    var projectId by remember { mutableStateOf("") }
    val cliRunning by cliLauncher.running.collectAsState()

    CarbonLayer {
        Column(
            Modifier
                .layerBackground()
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            BasicText(
                text = "CLI",
                style = Carbon.typography.heading03.copy(
                    Carbon.theme.textPrimary
                )
            )
            BasicText(
                text = "Enter a folder path to run the CLI tool on",
                style = Carbon.typography.body01.copy(
                    Carbon.theme.textPrimary
                )
            )
            Spacer(Modifier.height(16.dp))
            TextInput(
                label = "Path",
                value = path,
                onValueChange = { path = it }
            )
            Spacer(Modifier.height(8.dp))
            TextInput(
                label = "Project ID",
                value = projectId,
                onValueChange = { projectId = it }
            )
            Spacer(Modifier.height(8.dp))
            SmallProgressButton(
                label = "Measure (code + module structure)",
                onClick = { cliLauncher.measure(path, ProjectId(projectId)) },
                loading = cliRunning,
                isEnabled = path.isNotBlank() && projectId.isNotBlank(),
                buttonType = ButtonType.Primary
            )
        }
    }
}