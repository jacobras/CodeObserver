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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import io.ktor.client.call.body
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import nl.jacobras.codebaseobserver.dto.ArtifactSizeDto
import nl.jacobras.codebaseobserver.dto.CodeMetricsDto
import nl.jacobras.codebaseobserver.dto.ProjectDto
import nl.jacobras.codebaseobserver.dto.ProjectRequest
import nl.jacobras.codebaseobserver.settings.SettingsScreen
import nl.jacobras.codebaseobserver.web.BuildConfig

@OptIn(ExperimentalCarbonApi::class)
@Composable
fun App() {
    var metrics by remember { mutableStateOf<List<CodeMetricsDto>>(emptyList()) }
    var artifactSizes by remember { mutableStateOf<List<ArtifactSizeDto>>(emptyList()) }
    var projects by remember { mutableStateOf<List<ProjectDto>>(emptyList()) }
    var selectedProjectId by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
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
    val scope = rememberCoroutineScope()

    suspend fun reloadProjects() {
        val latestProjects: List<ProjectDto> = client.get("/projects").body()
        projects = latestProjects
        selectedProjectId = when {
            latestProjects.isEmpty() -> ""
            selectedProjectId.isBlank() -> latestProjects.first().projectId
            latestProjects.any { it.projectId == selectedProjectId } -> selectedProjectId
            else -> latestProjects.first().projectId
        }
    }

    suspend fun reloadMetrics() {
        if (selectedProjectId.isBlank()) {
            metrics = emptyList()
            artifactSizes = emptyList()
            return
        }
        metrics = client.get("/metrics") {
            url { parameters.append("projectId", selectedProjectId) }
        }.body()
        artifactSizes = client.get("/artifactSizes") {
            url { parameters.append("projectId", selectedProjectId) }
        }.body()
    }

    DisposableEffect(Unit) {
        onDispose { client.close() }
    }

    LaunchedEffect(Unit) {
        try {
            reloadProjects()
        } catch (e: Throwable) {
            error = e.message ?: "Failed to load"
        }
    }

    LaunchedEffect(selectedProjectId) {
        try {
            reloadMetrics()
        } catch (e: Throwable) {
            error = e.message ?: "Failed to load"
        }
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
                textColor = Color(DefaultColors.Light.textColor),
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
                                metrics = metrics,
                                artifactSizes = artifactSizes,
                                error = error,
                                projects = projects,
                                selectedProjectId = selectedProjectId,
                                onSelectProject = { selectedProjectId = it.trim() },
                                onDelete = { record ->
                                    scope.launch {
                                        error = null
                                        try {
                                            client.delete("/metrics/${record.gitHash}") {
                                                url { parameters.append("projectId", selectedProjectId) }
                                            }
                                            reloadProjects()
                                            reloadMetrics()
                                        } catch (e: Throwable) {
                                            error = e.message ?: "Failed to delete"
                                        }
                                    }
                                },
                                client = client
                            )
                        }
                        Screen.Settings -> {
                            SettingsScreen(
                                projects = projects,
                                error = error,
                                onSaveProject = { projectId, name ->
                                    scope.launch {
                                        error = null
                                        try {
                                            client.post("/projects") {
                                                contentType(ContentType.Application.Json)
                                                setBody(
                                                    ProjectRequest(
                                                        projectId = projectId.trim(),
                                                        name = name.trim()
                                                    )
                                                )
                                            }
                                            reloadProjects()
                                            reloadMetrics()
                                        } catch (e: Throwable) {
                                            error = e.message ?: "Failed to save project"
                                        }
                                    }
                                },
                                onDeleteProject = { projectId ->
                                    scope.launch {
                                        error = null
                                        try {
                                            client.delete("/projects/${projectId.trim()}")
                                            reloadProjects()
                                            reloadMetrics()
                                        } catch (e: Throwable) {
                                            error = e.message ?: "Failed to delete project"
                                        }
                                    }
                                }
                            )
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