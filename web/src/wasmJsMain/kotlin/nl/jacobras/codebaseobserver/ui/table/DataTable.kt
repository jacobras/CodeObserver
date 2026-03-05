package nl.jacobras.codebaseobserver.ui.table

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.foundation.color.borderSubtleColor
import com.gabrieldrn.carbon.foundation.color.layerAccentColor
import com.gabrieldrn.carbon.foundation.color.layerBackgroundColor
import com.gabrieldrn.carbon.foundation.spacing.SpacingScale

@Composable
internal fun DataTable(
    columnHeadings: List<String>,
    rowCount: Int,
    cellContent: @Composable (rowIndex: Int, columnIndex: Int, modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Carbon.theme.layerAccentColor())
                .padding(horizontal = SpacingScale.spacing05)
        ) {
            for (heading in columnHeadings) {
                BasicText(
                    text = heading,
                    style = Carbon.typography.headingCompact01,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        LazyColumn {
            items(rowCount) { rowIndex ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Carbon.theme.layerBackgroundColor())
                        .padding(horizontal = SpacingScale.spacing05),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (columnIndex in columnHeadings.indices) {
                        cellContent(
                            rowIndex,
                            columnIndex,
                            Modifier.weight(1f)
                        )
                    }
                }
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Carbon.theme.borderSubtleColor())
                )
            }
        }
    }
}