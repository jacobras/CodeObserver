package nl.jacobras.codebaseobserver.util.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.button.Button
import com.gabrieldrn.carbon.button.ButtonSize
import com.gabrieldrn.carbon.button.ButtonType
import com.gabrieldrn.carbon.foundation.color.CarbonLayer
import com.gabrieldrn.carbon.foundation.color.layerBackground

@Composable
internal fun DeleteDialog(
    message: String,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(
        onDismissRequest = onCancel,
        content = {
            CarbonLayer {
                Column(
                    modifier = Modifier
                        .layerBackground()
                        .padding(16.dp)
                ) {
                    BasicText(
                        text = message,
                        style = Carbon.typography.body02.copy(color = Carbon.theme.textPrimary)
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            label = "Cancel",
                            buttonType = ButtonType.Secondary,
                            buttonSize = ButtonSize.Small,
                            onClick = onCancel
                        )
                        Button(
                            label = "Delete",
                            buttonType = ButtonType.PrimaryDanger,
                            buttonSize = ButtonSize.Small,
                            onClick = onDelete
                        )
                    }
                }
            }
        }
    )

}