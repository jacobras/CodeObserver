package nl.jacobras.codeobserver.desktop

import com.github.ajalt.clikt.core.main
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import nl.jacobras.codeobserver.cli.command.measure.MeasureCommand
import nl.jacobras.codeobserver.dto.ProjectId

internal class CliLauncher {
    val scope = CoroutineScope(Dispatchers.IO)
    val running = MutableStateFlow(false)

    fun measure(
        path: String,
        projectId: ProjectId
    ) = scope.launch {
        running.value = true
        runCatching {
            MeasureCommand().main(
                arrayOf(
                    "--path", path,
                    "--project", projectId.value,
                    "--server", "http://localhost:8080"
                )
            )
        }.onSuccess {
            running.value = false
        }.onFailure {
            running.value = false
        }
    }
}