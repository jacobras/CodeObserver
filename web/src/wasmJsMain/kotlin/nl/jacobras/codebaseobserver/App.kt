package nl.jacobras.codebaseobserver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.CarbonDesignSystem
import com.gabrieldrn.carbon.api.ExperimentalCarbonApi
import com.gabrieldrn.carbon.button.Button
import com.gabrieldrn.carbon.button.ButtonType
import com.gabrieldrn.carbon.foundation.color.WhiteTheme
import com.patrykandpatrick.vico.compose.common.DefaultColors
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.VicoTheme
import com.patrykandpatrick.vico.compose.common.VicoTheme.CandlestickCartesianLayerColors
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import nl.jacobras.codebaseobserver.settings.SettingsScreen
import nl.jacobras.codebaseobserver.web.BuildConfig

@OptIn(ExperimentalCarbonApi::class)
@Composable
fun App() {
    var activeScreen by remember { mutableStateOf(Screen.Dashboard) }

    val client = remember {
        HttpClient(Js) {
            defaultRequest {
                url("/")
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
    val viewModel = remember { AppViewModel(client) }
    val projects by viewModel.projects.collectAsState(emptyList())
    val selectedProjectId by viewModel.selectedProjectId.collectAsState("")
    val loadingError by viewModel.loadingError.collectAsState("")

    DisposableEffect(Unit) {
        onDispose { client.close() }
    }

    CarbonDesignSystem(
        theme = WhiteTheme.copy(
            borderInteractive = Color(0xFF1F3D4D),
            layerSelectedInverse = Color(0xFF1F3D4D)
        )
    ) {
        ProvideVicoTheme(
            theme = VicoTheme(
                candlestickCartesianLayerColors =
                    CandlestickCartesianLayerColors.fromDefaultColors(DefaultColors.Light),
                columnCartesianLayerColors = DefaultColors.Light.cartesianLayerColors.map(::Color),
                lineColor = Color(DefaultColors.Light.lineColor),
                textColor = Carbon.theme.textPrimary,
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Carbon.theme.background)
            ) {
                TopNav(
                    active = activeScreen,
                    onSelect = { activeScreen = it }
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    when (activeScreen) {
                        Screen.Dashboard -> {
                            DashboardScreen(
                                error = loadingError,
                                projects = projects,
                                selectedProjectId = selectedProjectId,
                                onSelectProject = { viewModel.selectProject(it.trim()) },
                                client = client
                            )
                        }
                        Screen.Settings -> {
                            SettingsScreen(client = client)
                        }
                    }
                }
            }
        }
    }
}

private enum class Screen(val label: String) {
    Dashboard("Dashboard"),
    Settings("Settings")
}

@Composable
private fun TopNav(active: Screen, onSelect: (Screen) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1F3D4D))
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicText(
            text = "CodebaseObserver ${BuildConfig.VERSION}",
            style = Carbon.typography.headingCompact02.copy(color = Color(0xFFF5F2EA))
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Screen.entries.forEach { screen ->
                val selected = screen == active
                Button(
                    label = screen.label,
                    buttonType = if (selected) ButtonType.Primary else ButtonType.Ghost,
                    buttonSize = com.gabrieldrn.carbon.button.ButtonSize.Small,
                    onClick = { onSelect(screen) }
                )
            }
        }
    }
}