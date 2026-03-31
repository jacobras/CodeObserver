package nl.jacobras.codeobserver.util.ui.chart

import androidx.compose.runtime.Composable
import com.gabrieldrn.carbon.contentswitcher.ContentSwitcher

@Composable
internal fun TimeViewSelector(
    selected: TimeView,
    onSelect: (TimeView) -> Unit
) {
    ContentSwitcher(
        options = TimeView.entries.map { it.label },
        selectedOption = selected.label,
        onOptionSelected = { newSelected ->
            onSelect(TimeView.entries.first { it.label == newSelected })
        }
    )
}