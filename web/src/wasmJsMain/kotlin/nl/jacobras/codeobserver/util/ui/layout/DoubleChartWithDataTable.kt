package nl.jacobras.codeobserver.util.ui.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
internal fun DoubleChartWithDataTable(
    firstChart: @Composable (Modifier) -> Unit,
    secondChart: @Composable (Modifier) -> Unit,
    dataTable: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier
) {
    val windowSize = calculateWindowSizeClass()
    val movableFirstChart = remember(firstChart) { movableContentOf(firstChart) }
    val movableSecondChart = remember(secondChart) { movableContentOf(secondChart) }
    val movableDataTable = remember(dataTable) { movableContentOf(dataTable) }

    val useHorizontalLayout = windowSize.widthSizeClass >= WindowWidthSizeClass.Expanded
    val chartHeight = when {
        windowSize.heightSizeClass >= WindowHeightSizeClass.Expanded -> 300.dp
        windowSize.heightSizeClass >= WindowHeightSizeClass.Medium -> 250.dp
        else -> 200.dp
    }

    if (useHorizontalLayout) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                movableFirstChart(Modifier.height(chartHeight))
                movableSecondChart(Modifier.height(chartHeight))
            }
            movableDataTable(Modifier.weight(1f))
        }
    } else {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            movableFirstChart(Modifier.height(chartHeight))
            movableSecondChart(Modifier.height(chartHeight))
            movableDataTable(Modifier)
        }
    }
}