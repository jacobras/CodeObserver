package nl.jacobras.codebaseobserver.ui.chart

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.Carbon
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DividerProperties
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
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
    val chartData = buildTimeChartData(
        records = records,
        timeView = timeView,
        getDate = dateField,
        getValue = metricField
    )
    val lineData = remember(records, timeView) {
        listOf(
            Line(
                label = title,
                color = SolidColor(color),
                values = chartData.yValues.map { it.toDouble() },
                firstGradientFillColor = color.copy(alpha = 0.35f),
                secondGradientFillColor = Color.Transparent,
                dotProperties = DotProperties(
                    enabled = true,
                    color = SolidColor(Color.White),
                    strokeColor = SolidColor(color),
                    radius = 5.dp,
                    strokeWidth = 3.dp
                )
            )
        )
    }

    LineChart(
        modifier = modifier.padding(horizontal = 12.dp),
        data = lineData,
        curvedEdges = false,
        animationMode = AnimationMode.None,
        gridProperties = GridProperties(
            enabled = true,
            xAxisProperties = GridProperties.AxisProperties(enabled = false),
            yAxisProperties = GridProperties.AxisProperties(
                enabled = true,
                thickness = 1.dp,
                color = SolidColor(Color(0xFFE0E0E0))
            )
        ),
        indicatorProperties = HorizontalIndicatorProperties(
            enabled = true,
            textStyle = Carbon.typography.bodyCompact01.copy(color = Color(0xFF2F4858)),
            padding = 12.dp
        ),
        labelProperties = LabelProperties(
            enabled = true,
            labels = chartData.xLabels,
            textStyle = Carbon.typography.bodyCompact01.copy(color = Color(0xFF2F4858)),
            rotation = LabelProperties.Rotation(degree = 0f)
        ),
        dividerProperties = DividerProperties(enabled = false)
    )
}