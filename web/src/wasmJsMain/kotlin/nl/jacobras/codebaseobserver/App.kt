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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.CarbonDesignSystem
import com.gabrieldrn.carbon.api.ExperimentalCarbonApi
import com.gabrieldrn.carbon.button.Button
import com.gabrieldrn.carbon.button.ButtonType
import com.gabrieldrn.carbon.foundation.color.WhiteTheme
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import nl.jacobras.codebaseobserver.dto.MetricsDto
import nl.jacobras.codebaseobserver.web.BuildConfig

@OptIn(ExperimentalCarbonApi::class)
@Composable
fun App() {
    var records by remember { mutableStateOf<List<MetricsDto>>(emptyList()) }
    var projectIds by remember { mutableStateOf<List<String>>(emptyList()) }
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
        projectIds = client.get("/projects").body()
        if (selectedProjectId.isBlank() && projectIds.isNotEmpty()) {
            selectedProjectId = projectIds.first()
        }
    }

    suspend fun reloadRecords() {
        if (selectedProjectId.isBlank()) {
            records = emptyList()
            return
        }
        records = client.get("/metrics") {
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
        if (selectedProjectId.isBlank()) return@LaunchedEffect
        try {
            reloadRecords()
        } catch (e: Throwable) {
            error = e.message ?: "Failed to load"
        }
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFF4F1EA), Color(0xFFE9F0F2))
    )

    CarbonDesignSystem(
        theme = WhiteTheme.copy(
            borderInteractive = Color(0xFF1F3D4D)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
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
                            records = records,
                            error = error,
                            projectIds = projectIds,
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
                                        reloadRecords()
                                    } catch (e: Throwable) {
                                        error = e.message ?: "Failed to delete"
                                    }
                                }
                            }
                        )
                    }
                    Screen.Settings -> {
                        SettingsScreen()
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