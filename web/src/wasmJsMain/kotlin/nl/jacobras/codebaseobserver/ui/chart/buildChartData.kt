package nl.jacobras.codebaseobserver.ui.chart

import nl.jacobras.codebaseobserver.CountRecord
import nl.jacobras.codebaseobserver.GradleRecord
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

internal fun buildChartData(
    records: List<CountRecord>,
    timeView: TimeView,
    clock: Clock = Clock.System
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

    val buckets = Array(bucketCount) { mutableListOf<CountRecord>() }

    // Store all data points in the allocated buckets
    for (record in records.filter { it.gitDate in windowStart..now }) {
        val offset = record.gitDate - windowStart
        val index = (offset / bucketSize).toInt()

        if (index in 0 until bucketCount) {
            buckets[index].add(record)
        }
    }

    val xLabels = mutableListOf<String>()
    val yValues = mutableListOf<Int>()

    // Find the last known value before the time window to start chart with the oldest known data.
    var lastValue = records
        .filter { it.gitDate < windowStart }
        .maxByOrNull { it.gitDate }
        ?.linesOfCode
        ?: 0

    for (i in 0 until bucketCount) {
        val bucketRecords = buckets[i]

        // Take the newest data point that fits a bucket
        val value = bucketRecords
            .maxByOrNull { it.gitDate }
            ?.linesOfCode
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

internal fun buildGradleChartData(
    records: List<GradleRecord>,
    timeView: TimeView,
    clock: Clock = Clock.System
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

    val buckets = Array(bucketCount) { mutableListOf<GradleRecord>() }

    // Store all data points in the allocated buckets
    for (record in records.filter { it.gitDate in windowStart..now }) {
        val offset = record.gitDate - windowStart
        val index = (offset / bucketSize).toInt()

        if (index in 0 until bucketCount) {
            buckets[index].add(record)
        }
    }

    val xLabels = mutableListOf<String>()
    val yValues = mutableListOf<Int>()

    // Find the last known value before the time window to start chart with the oldest known data.
    var lastValue = records
        .filter { it.gitDate < windowStart }
        .maxByOrNull { it.gitDate }
        ?.moduleCount
        ?: 0

    for (i in 0 until bucketCount) {
        val bucketRecords = buckets[i]

        // Take the newest data point that fits a bucket
        val value = bucketRecords
            .maxByOrNull { it.gitDate }
            ?.moduleCount
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

internal fun buildModuleTreeHeightChartData(
    records: List<GradleRecord>,
    timeView: TimeView,
    clock: Clock = Clock.System
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

    val buckets = Array(bucketCount) { mutableListOf<GradleRecord>() }

    // Store all data points in the allocated buckets
    for (record in records.filter { it.gitDate in windowStart..now }) {
        val offset = record.gitDate - windowStart
        val index = (offset / bucketSize).toInt()

        if (index in 0 until bucketCount) {
            buckets[index].add(record)
        }
    }

    val xLabels = mutableListOf<String>()
    val yValues = mutableListOf<Int>()

    // Find the last known value before the time window to start chart with the oldest known data.
    var lastValue = records
        .filter { it.gitDate < windowStart }
        .maxByOrNull { it.gitDate }
        ?.moduleTreeHeight
        ?: 0

    for (i in 0 until bucketCount) {
        val bucketRecords = buckets[i]

        // Take the newest data point that fits a bucket
        val value = bucketRecords
            .maxByOrNull { it.gitDate }
            ?.moduleTreeHeight
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
