package nl.jacobras.codebaseobserver.ui.chart

import assertk.assertThat
import assertk.assertions.isEqualTo
import nl.jacobras.codebaseobserver.CountRecord
import nl.jacobras.codebaseobserver.TimeView
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class ChartDataBuilderTest {

    @Test
    fun last7Days() {
        val records = listOf(
            CountRecord(
                gitHash = "a",
                gitDate = Clock.System.now().minus(1.minutes),
                fileCount = 3,
                createdAt = "b"
            )
        )

        val data = buildChartData(records, TimeView.Last7Days)

        assertThat(data.xLabels).isEqualTo(
            listOf(
                "6 days ago",
                "5 days ago",
                "4 days ago",
                "3 days ago",
                "2 days ago",
                "Yesterday",
                "Today"
            )
        )
        assertThat(data.yValues).isEqualTo(
            listOf(0, 0, 0, 0, 0, 0, 3)
        )
    }

    @Test
    fun last30Days() {
        val records = listOf(
            CountRecord(
                gitHash = "a",
                gitDate = Clock.System.now().minus(1.minutes),
                fileCount = 3,
                createdAt = "b"
            ),
            CountRecord(
                gitHash = "a",
                gitDate = Clock.System.now().minus(5.days),
                fileCount = 8,
                createdAt = "b"
            )
        )

        val data = buildChartData(records, TimeView.Last30Days)

        assertThat(data.xLabels).isEqualTo(
            listOf(
                "", "", "27 days ago",
                "", "", "24 days ago",
                "", "", "21 days ago",
                "", "", "18 days ago",
                "", "", "15 days ago",
                "", "", "12 days ago",
                "", "", "9 days ago",
                "", "", "6 days ago",
                "", "", "3 days ago",
                "", "", "Today"
            )
        )
        assertThat(data.yValues).isEqualTo(
            List(25) { 0 } + List(4) { 8 } + listOf(3)
        )
    }

    @Test
    fun last6Months() {
        val records = listOf(
            CountRecord(
                gitHash = "a",
                gitDate = Clock.System.now().minus(90.days),
                fileCount = 3,
                createdAt = "b"
            ),
            CountRecord(
                gitHash = "a",
                gitDate = Clock.System.now().minus(10.days),
                fileCount = 22,
                createdAt = "b"
            )
        )

        val data = buildChartData(records, TimeView.Last6Months)

        assertThat(data.xLabels).isEqualTo(
            listOf(
                "5 months ago",
                "4 months ago",
                "3 months ago",
                "2 months ago",
                "1 month ago",
                "This month"
            )
        )
        assertThat(data.yValues).isEqualTo(
            listOf(0, 0, 0, 3, 3, 22)
        )
    }

    @Test
    fun last12Months() {
        val records = listOf(
            CountRecord(
                gitHash = "a",
                gitDate = Clock.System.now().minus(9.days),
                fileCount = 3,
                createdAt = "b"
            ),
            CountRecord(
                gitHash = "a",
                gitDate = Clock.System.now().minus(50.days),
                fileCount = 22,
                createdAt = "b"
            ),
            CountRecord(
                gitHash = "a",
                gitDate = Clock.System.now().minus(200.days),
                fileCount = 88,
                createdAt = "b"
            )
        )

        val data = buildChartData(records, TimeView.Last12Months)

        assertThat(data.xLabels).isEqualTo(
            listOf(
                "", "10 months ago",
                "", "8 months ago",
                "", "6 months ago",
                "", "4 months ago",
                "", "2 months ago",
                "1 month ago",
                "This month"
            )
        )
        assertThat(data.yValues).isEqualTo(
            listOf(0, 0, 0, 0, 0, 88, 88, 88, 88, 88, 22, 3)
        )
    }
}