package nl.jacobras.codeobserver.util.ui.chart

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import com.gabrieldrn.carbon.contentswitcher.ContentSwitcher
import com.gabrieldrn.carbon.contentswitcher.ContentSwitcherSize

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
internal fun TimeViewSelector(
    selected: TimeView,
    onSelect: (TimeView) -> Unit
) {
    val windowSize = calculateWindowSizeClass()
    val largeHorizontally = windowSize.widthSizeClass >= WindowWidthSizeClass.Expanded
    val largeVertically = windowSize.heightSizeClass >= WindowHeightSizeClass.Expanded

    ContentSwitcher(
        options = TimeView.entries.map {
            if (largeHorizontally) {
                it.largeLabel
            } else {
                it.smallLabel
            }
        },
        selectedOption = if (largeHorizontally) {
            selected.largeLabel
        } else {
            selected.smallLabel
        },
        onOptionSelected = { newSelected ->
            val timeView = TimeView.entries.first {
                if (largeHorizontally) {
                    it.largeLabel == newSelected
                } else {
                    it.smallLabel == newSelected
                }
            }
            onSelect(timeView)
        },
        size = if (largeVertically) {
            ContentSwitcherSize.Medium
        } else {
            ContentSwitcherSize.Small
        }
    )
}