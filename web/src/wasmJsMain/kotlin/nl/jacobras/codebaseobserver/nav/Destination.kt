package nl.jacobras.codebaseobserver.nav

internal enum class Screen(
    val route: String,
    val label: String
) {
    Dashboard("dashboard", "Dashboard"),
    Settings("settings", "Settings");

    companion object {
        fun fromRoute(route: String): Screen? = entries.firstOrNull { it.route == route }
    }
}