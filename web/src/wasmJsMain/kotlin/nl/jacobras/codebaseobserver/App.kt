package nl.jacobras.codebaseobserver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.api.ExperimentalCarbonApi
import com.gabrieldrn.carbon.button.Button
import com.gabrieldrn.carbon.button.ButtonSize
import com.gabrieldrn.carbon.button.ButtonType
import nl.jacobras.codebaseobserver.dashboard.DashboardScreen
import nl.jacobras.codebaseobserver.settings.SettingsScreen
import nl.jacobras.codebaseobserver.util.ui.theme.COTheme
import nl.jacobras.codebaseobserver.web.BuildConfig

@OptIn(ExperimentalCarbonApi::class)
@Composable
fun App(
    onNavHostReady: suspend (NavController) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val activeScreen = navBackStackEntry?.destination?.route?.let { Screen.fromRoute(it) } ?: Screen.Dashboard

    COTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Carbon.theme.background)
        ) {
            TopNav(
                active = activeScreen,
                onSelect = { navController.navigate(it.route) }
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {

                NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
                    composable(Screen.Dashboard.route) { DashboardScreen() }
                    composable(Screen.Settings.route) { SettingsScreen() }
                }
            }
        }
    }

    LaunchedEffect(navController) {
        onNavHostReady(navController)
    }
}

@Composable
private fun TopNav(active: Screen, onSelect: (Screen) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1F3D4D))
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val demoSuffix = if (BuildConfig.IS_DEMO) {
            " (DEMO)"
        } else {
            ""
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {}
        BasicText(
            text = "CodebaseObserver ${BuildConfig.VERSION}$demoSuffix",
            style = Carbon.typography.headingCompact02.copy(color = Color(0xFFF5F2EA))
        )
        Spacer(Modifier.weight(1f))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Screen.entries.forEach { screen ->
                val selected = screen == active
                Button(
                    label = screen.label,
                    buttonType = if (selected) ButtonType.Primary else ButtonType.Ghost,
                    buttonSize = ButtonSize.Small,
                    onClick = { onSelect(screen) }
                )
            }
        }
    }
}