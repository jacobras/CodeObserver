package nl.jacobras.codeobserver.dashboard

internal enum class DashboardDestination(
    val route: String,
    val label: String
) {
    CodeTrends("codeTrends", "Code trends"),
    Artifacts("artifacts", "Artifact sizes"),
    BuildTimes("buildTimes", "Build times"),
    Migrations("migrations", "Migrations"),
    DetektTrends("detektTrends", "Detekt trends"),
    DetektReport("detektReport", "Detekt report"),
    ModuleGraph("moduleGraph", "Module graph");

    companion object {
        fun fromRoute(route: String) = entries.firstOrNull { it.route == route }
        fun fromLabel(label: String) = entries.firstOrNull { it.label == label }
    }
}