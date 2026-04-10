package nl.jacobras.codeobserver.util.ui.commandinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.Carbon
import kotlinx.browser.window
import nl.jacobras.codeobserver.dto.ProjectId

@Composable
internal fun CommandExample(
    command: String,
    projectId: ProjectId,
    modifier: Modifier = Modifier
) {
    val serverUrl = window.location.origin

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BasicText(
            text = "To upload data, run the following command:",
            style = Carbon.typography.body01.copy(
                color = Carbon.theme.textPrimary
            )
        )
        SelectionContainer {
            BasicText(
                text = buildAnnotatedString {
                    append("java -jar code-observer.jar")
                    withStyle(
                        SpanStyle(fontWeight = FontWeight.Bold)
                    ) {
                        append(" $command")
                    }
                    append(" --project=${projectId.value}")
                    append(" --server=$serverUrl")
                },
                style = Carbon.typography.code01.copy(
                    color = Carbon.theme.textSecondary
                )
            )
        }
    }
}