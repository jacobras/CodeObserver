package nl.jacobras.codeobserver.util.ui.chart

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.Carbon
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberFadingEdges
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.LegendItem
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend
import com.patrykandpatrick.vico.compose.common.vicoTheme
import io.github.z4kn4fein.semver.Version
import nl.jacobras.humanreadable.HumanReadable

@Composable
internal fun <T> VersionChart(
    title: String,
    records: List<T>,
    versionField: (T) -> Version,
    metricField: (T) -> Long,
    color: Color,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val legendLabelComponent = rememberTextComponent(TextStyle(vicoTheme.textColor))

    if (records.isEmpty()) {
        BasicText(
            text = "No data",
            style = Carbon.typography.body02.copy(color = Carbon.theme.textPrimary)
        )
        return
    }

    LaunchedEffect(records) {
        modelProducer.runTransaction {
            columnSeries {
                series(
                    x = records.indices.map { it.toDouble() },
                    y = records.map(metricField)
                )
            }
        }
    }

    CartesianChartHost(
        modifier = modifier,
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(fill = Fill(color))
                )
            ),
            startAxis = VerticalAxis.rememberStart(
                valueFormatter = CartesianValueFormatter { _, y, _ ->
                    HumanReadable.fileSize(y.toLong())
                }
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = CartesianValueFormatter { _, x, _ ->
                    val index = x.toInt()
                    versionField(records[index]).toString()
                }),
            fadingEdges = rememberFadingEdges(),
            legend = rememberHorizontalLegend(
                items = {
                    add(
                        LegendItem(
                            icon = ShapeComponent(Fill(color), CircleShape),
                            labelComponent = legendLabelComponent,
                            label = title
                        )
                    )
                },
                padding = Insets(top = 16.dp)
            )
        ),
        modelProducer = modelProducer,
        animateIn = false,
        animationSpec = null,
        zoomState = rememberVicoZoomState(zoomEnabled = false, initialZoom = Zoom.Content),
        scrollState = rememberVicoScrollState(scrollEnabled = false)
    )
}