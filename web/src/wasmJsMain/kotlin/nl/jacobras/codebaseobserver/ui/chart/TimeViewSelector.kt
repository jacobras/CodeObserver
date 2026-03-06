package nl.jacobras.codebaseobserver.ui.chart

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
        onOptionSelected = { selected ->
            onSelect(TimeView.entries.first { it.label == selected })
        }
    )
}