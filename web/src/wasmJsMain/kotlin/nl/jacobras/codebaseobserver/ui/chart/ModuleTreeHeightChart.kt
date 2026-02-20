package nl.jacobras.codebaseobserver.ui.chart

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DividerProperties
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import nl.jacobras.codebaseobserver.GradleRecord

@Composable
internal fun ModuleTreeHeightChart(records: List<GradleRecord>, timeView: TimeView) {
    val chartData = buildModuleTreeHeightChartData(records, timeView)
    val lineData = remember(records, timeView) {
        listOf(
            Line(
                label = "Module tree height",
                color = SolidColor(Color(0xFF264653)),
                values = chartData.yValues.map { it.toDouble() },
                firstGradientFillColor = Color(0xFF264653).copy(alpha = 0.35f),
                secondGradientFillColor = Color.Transparent,
                dotProperties = DotProperties(
                    enabled = true,
                    color = SolidColor(Color.White),
                    strokeColor = SolidColor(Color(0xFF264653)),
                    radius = 6.dp,
                    strokeWidth = 3.dp
                )
            )
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        LineChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(horizontal = 12.dp),
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
                textStyle = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF2F4858)),
                padding = 12.dp
            ),
            labelProperties = LabelProperties(
                enabled = true,
                labels = chartData.xLabels,
                textStyle = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF2F4858)),
                rotation = LabelProperties.Rotation(degree = 0f)
            ),
            dividerProperties = DividerProperties(enabled = false)
        )
    }
}

