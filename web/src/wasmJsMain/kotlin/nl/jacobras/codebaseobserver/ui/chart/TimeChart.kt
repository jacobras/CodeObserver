package nl.jacobras.codebaseobserver.ui.chart

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.Carbon
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberFadingEdges
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.LegendItem
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend
import com.patrykandpatrick.vico.compose.common.vicoTheme
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import nl.jacobras.humanreadable.HumanReadable
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

@Composable
internal fun <T> TimeChart(
    title: String,
    records: List<T>,
    dateField: (T) -> Instant,
    metricField: (T) -> Int,
    color: Color,
    timeView: TimeView,
    modifier: Modifier = Modifier
) {
    val now = Clock.System.now()
    val filteredRecords = remember(records, timeView) {
        records.filter(timeView, dateField)
    }

    val timeZone = TimeZone.currentSystemDefault()
    fun Instant.date(): LocalDate {
        return toLocalDateTime(timeZone).date
    }

    val range = remember(timeView) {
        val (start, end) = timeView.getRange()
        start.date().toEpochDays().toDouble() to end.date().toEpochDays().toDouble()
    }
    val rangeKey = ExtraStore.Key<Pair<Double, Double>>()
    val rangeProvider = object : CartesianLayerRangeProvider {
        override fun getMinX(minX: Double, maxX: Double, extraStore: ExtraStore): Double {
            return extraStore.getOrNull(rangeKey)?.first ?: 0.0
        }

        override fun getMaxX(
            minX: Double,
            maxX: Double,
            extraStore: ExtraStore
        ): Double {
            return now.date().toEpochDays().toDouble()
        }
    }

    if (filteredRecords.isEmpty()) {
        BasicText(
            modifier = modifier,
            text = "No data",
            style = Carbon.typography.body02.copy(color = Carbon.theme.textPrimary)
        )
        return
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(filteredRecords, timeView) {
        modelProducer.runTransaction {
            lineSeries {
                series(
                    x = filteredRecords.map { dateField(it).date().toEpochDays() },
                    y = filteredRecords.map(metricField)
                )
            }
            extras { it[rangeKey] = range }
        }
    }

    // Formats the x-axis values, where [x] will be real values from records but also generated in-between values.
    val today = Clock.System.now().date()
    val yesterday = today.minus(DatePeriod(days = 1))
    val formatter = CartesianValueFormatter { _, x, _ ->
        val epochDays = x.toLong()
        when (val date = LocalDate.fromEpochDays(epochDays)) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> date.toString()
        }
    }

    val legendLabelComponent = rememberTextComponent(TextStyle(vicoTheme.textColor))

    CartesianChartHost(
        modifier = modifier,
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(Fill(color)),
                        areaFill = LineCartesianLayer.AreaFill.single(
                            Fill(
                                Brush.verticalGradient(listOf(color.copy(alpha = 0.4f), Color.Transparent))
                            )
                        ),
                        pointProvider = LineCartesianLayer.PointProvider.single(
                            LineCartesianLayer.Point(rememberShapeComponent(Fill(color), CircleShape))
                        )
                    )
                ),
                rangeProvider = rangeProvider
            ),
            startAxis = VerticalAxis.rememberStart(
                valueFormatter = CartesianValueFormatter { _, y, _ ->
                    HumanReadable.number(y)
                }
            ),
            bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = formatter),
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

@Suppress("MagicNumber")
private fun <T> List<T>.filter(
    timeView: TimeView,
    getDate: (T) -> Instant
): List<T> {
    val now = Clock.System.now()

    val rangeStart: Instant = when (timeView) {
        TimeView.Last7Days -> now.minus(7.days)
        TimeView.Last30Days -> now.minus(30.days)
        TimeView.Last6Months -> now.minus(180.days)
        TimeView.Last12Months -> now.minus(365.days)
    }

    val withinRange = filter { getDate(it) >= rangeStart }

    val anchorPoint = filter { getDate(it) < rangeStart }
        .maxByOrNull { getDate(it) }

    return if (anchorPoint != null) {
        listOf(anchorPoint) + withinRange
    } else {
        withinRange
    }
}

private fun TimeView.getRange(): Pair<Instant, Instant> {
    val now = Clock.System.now()
    val start = when (this) {
        TimeView.Last7Days -> now.minus(7.days)
        TimeView.Last30Days -> now.minus(30.days)
        TimeView.Last6Months -> now.minus(180.days)
        TimeView.Last12Months -> now.minus(365.days)
    }.plus(1.days)
    return start to now
}