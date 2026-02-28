package nl.jacobras.codebaseobserver.ui.carbon

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.button.Button
import com.gabrieldrn.carbon.foundation.spacing.SpacingScale

/**
 * From https://github.com/gabrieldrn/carbon-compose/blob/main/catalog/src/commonMain/kotlin/com/gabrieldrn/carbon/catalog/common/IntSelector.kt,
 * but adapted to use regular [Button]s and to accept a List<Int> instead of a range.
 */
@Composable
internal fun IntSelector(
    label: String,
    value: Int,
    values: List<Int>,
    onValueChanged: (Int) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        BasicText(
            text = label,
            style = Carbon.typography.label01.copy(color = Carbon.theme.textSecondary),
        )

        Row(
            modifier = Modifier.padding(top = SpacingScale.spacing04),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val lessButtonEnabled by remember(value) {
                mutableStateOf(value > values.first())
            }
            val moreButtonEnabled by remember(value) {
                mutableStateOf(value < values.last())
            }

            Button(
                label = "-",
                onClick = {
                    val current = values.indexOf(value)
                    val previous = values.getOrNull(current - 1) ?: values.first()
                    onValueChanged(previous)
                },
                isEnabled = enabled && lessButtonEnabled
            )

            BasicText(
                text = value.toString(),
                style = Carbon.typography.body01
                    .copy(
                        color = if (enabled) Carbon.theme.textPrimary else Carbon.theme.textDisabled,
                        textAlign = TextAlign.Center
                    ),
                modifier = Modifier.width(SpacingScale.spacing09)
            )

            Button(
                label = "-",
                onClick = {
                    val current = values.indexOf(value)
                    val next = values.getOrNull(current + 1) ?: values.last()
                    onValueChanged(next)
                },
                isEnabled = enabled && moreButtonEnabled
            )
        }
    }
}