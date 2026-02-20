package nl.jacobras.codebaseobserver.ui.chart

import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

@Suppress("MagicNumber")
internal fun <T> buildChartData(
    records: List<T>,
    timeView: TimeView,
    clock: Clock = Clock.System,
    getDate: (T) -> Instant,
    getValue: (T) -> Int
): ChartData {
    val now = clock.now()

    val (bucketCount, windowDuration) = when (timeView) {
        TimeView.Last7Days -> 7 to 7.days
        TimeView.Last30Days -> 30 to 30.days
        TimeView.Last6Months -> 6 to 180.days
        TimeView.Last12Months -> 12 to 365.days
    }

    val bucketSize = windowDuration / bucketCount
    val windowStart = now - windowDuration

    val buckets = Array(bucketCount) { mutableListOf<T>() }

    // Store all data points in the allocated buckets
    for (record in records.filter { getDate(it) in windowStart..now }) {
        val offset = getDate(record) - windowStart
        val index = (offset / bucketSize).toInt()

        if (index in 0 until bucketCount) {
            buckets[index].add(record)
        }
    }

    val xLabels = mutableListOf<String>()
    val yValues = mutableListOf<Int>()

    // Find the last known value before the time window to start chart with the oldest known data.
    var lastValue = records
        .filter { getDate(it) < windowStart }
        .maxByOrNull { getDate(it) }
        ?.let { getValue(it) }
        ?: 0

    for (i in 0 until bucketCount) {
        val bucketRecords = buckets[i]

        // Take the newest data point that fits a bucket
        val value = bucketRecords
            .maxByOrNull { getDate(it) }
            ?.let { getValue(it) }
            ?: lastValue

        lastValue = value
        yValues += value
        xLabels += when (timeView) {
            TimeView.Last12Months -> {
                when {
                    i == 0 -> "This month"
                    i == 1 -> "1 month ago"
                    i % 2 == 0 -> "$i months ago"
                    else -> ""
                }
            }
            TimeView.Last6Months ->
                when (i) {
                    0 -> "This month"
                    1 -> "1 month ago"
                    else -> "$i months ago"
                }
            TimeView.Last30Days -> {
                when {
                    i == 0 -> "Today"
                    i % 3 == 0 -> "$i days ago"
                    else -> ""
                }
            }
            else -> {
                when (i) {
                    0 -> "Today"
                    1 -> "Yesterday"
                    else -> "$i days ago"
                }
            }
        }
    }

    return ChartData(
        xLabels = xLabels.reversed(),
        yValues = yValues
    )
}