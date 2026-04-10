package nl.jacobras.codeobserver.util.ui.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.Carbon
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.codeobserver.util.ui.commandinfo.CommandExample

@Composable
internal fun EmptyState(
    text: String,
    command: String,
    projectId: ProjectId
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BasicText(
            text = text,
            style = Carbon.typography.body02
        )

        CommandExample(command = command, projectId = projectId)
    }
}