package nl.jacobras.codeobserver.util.ui.commandinfo

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.button.Button
import com.gabrieldrn.carbon.button.ButtonSize
import com.gabrieldrn.carbon.button.ButtonType
import com.gabrieldrn.carbon.popover.carettip.PopoverCaretTipAlignment
import com.gabrieldrn.carbon.popover.carettip.PopoverCaretTipBox
import com.gabrieldrn.carbon.popover.carettip.PopoverCaretTipPlacement
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.codeobserver.web.generated.resources.Res
import nl.jacobras.codeobserver.web.generated.resources.play_box_outline
import org.jetbrains.compose.resources.painterResource

/**
 * @param command The command to show in the popover, e.g. "measure".
 */
@Composable
internal fun CommandInfoBox(
    command: String,
    projectId: ProjectId
) {
    var showPopover by remember { mutableStateOf(false) }

    PopoverCaretTipBox(
        isVisible = showPopover,
        content = {
            Button(
                label = "CLI command",
                iconPainter = painterResource(Res.drawable.play_box_outline),
                buttonSize = ButtonSize.Medium,
                buttonType = ButtonType.Tertiary,
                onClick = { showPopover = !showPopover }
            )
        },
        alignment = PopoverCaretTipAlignment.End,
        placement = PopoverCaretTipPlacement.Bottom,
        popoverContent = {
            CommandExample(
                command = command,
                projectId = projectId,
                modifier = Modifier.padding(16.dp)
            )
        },
        onDismissRequest = { showPopover = false }
    )
}