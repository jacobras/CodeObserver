package nl.jacobras.codeobserver

internal enum class Destination(
    val route: String,
    val label: String
) {
    Dashboard("dashboard", "Dashboard"),
    Users("users", "Users"),
    Settings("settings", "Settings");

    companion object {
        fun fromRoute(route: String): Destination? = entries.firstOrNull { it.route == route }
    }
}