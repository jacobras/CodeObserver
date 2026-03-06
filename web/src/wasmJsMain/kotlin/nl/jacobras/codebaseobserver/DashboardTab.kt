package nl.jacobras.codebaseobserver

internal enum class DashboardTab(val displayName: String) {
    CodeTrends("Code trends"),
    Artifacts("Artifact sizes"),
    Migrations("Migrations"),
    ModuleGraph("Module graph"),
    ModuleRules("Module rules"),
}