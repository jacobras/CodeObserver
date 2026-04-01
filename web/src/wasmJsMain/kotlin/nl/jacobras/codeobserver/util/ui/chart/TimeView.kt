package nl.jacobras.codeobserver.util.ui.chart

internal enum class TimeView(val largeLabel: String, val smallLabel: String) {
    Last7Days("Last 7 days", "7d"),
    Last30Days("Last 30 days", "30d"),
    Last3Months("Last 3 months", "3m"),
    Last6Months("Last 6 months", "6m"),
    Last12Months("Last 12 months", "12m")
}