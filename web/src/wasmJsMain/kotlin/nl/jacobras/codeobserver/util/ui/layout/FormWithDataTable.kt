package nl.jacobras.codeobserver.util.ui.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Responsive layout for a configuration form plus the data table it manages. On wide (Expanded)
 * screens the form sits on the left and the table on the right (50/50); on smaller screens they
 * stack, form above table.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
internal fun FormWithDataTable(
    form: @Composable (Modifier) -> Unit,
    dataTable: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier
) {
    val windowSize = calculateWindowSizeClass()
    val movableForm = remember(form) { movableContentOf(form) }
    val movableDataTable = remember(dataTable) { movableContentOf(dataTable) }

    val useHorizontalLayout = windowSize.widthSizeClass >= WindowWidthSizeClass.Expanded

    if (useHorizontalLayout) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            movableForm(Modifier.weight(1f))
            movableDataTable(Modifier.weight(1f))
        }
    } else {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            movableForm(Modifier)
            movableDataTable(Modifier)
        }
    }
}