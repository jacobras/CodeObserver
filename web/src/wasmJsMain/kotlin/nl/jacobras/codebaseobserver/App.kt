package nl.jacobras.codebaseobserver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import nl.jacobras.codebaseobserver.web.BuildConfig

@Composable
fun App() {
    var records by remember { mutableStateOf<List<CountRecord>>(emptyList()) }
    var gradleRecords by remember { mutableStateOf<List<GradleRecord>>(emptyList()) }
    var projectIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedProjectId by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var gitHashInput by remember { mutableStateOf("") }
    var gitDateInput by remember { mutableStateOf("") }
    var linesOfCodeInput by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
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

    fun resetForm() {
        gitHashInput = ""
        gitDateInput = ""
        linesOfCodeInput = ""
        isEditing = false
    }

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
        records = client.get("/counts") {
            url { parameters.append("projectId", selectedProjectId) }
        }.body()
    }

    suspend fun reloadGradleRecords() {
        if (selectedProjectId.isBlank()) {
            gradleRecords = emptyList()
            return
        }
        gradleRecords = client.get("/gradle") {
            url { parameters.append("projectId", selectedProjectId) }
        }.body()
    }

    DisposableEffect(Unit) {
        onDispose { client.close() }
    }

    LaunchedEffect(Unit) {
        try {
            reloadProjects()
            reloadRecords()
            reloadGradleRecords()
        } catch (ex: Throwable) {
            error = ex.message ?: "Failed to load"
        }
    }

    LaunchedEffect(selectedProjectId) {
        if (selectedProjectId.isBlank()) return@LaunchedEffect
        try {
            reloadRecords()
            reloadGradleRecords()
        } catch (ex: Throwable) {
            error = ex.message ?: "Failed to load"
        }
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFF4F1EA), Color(0xFFE9F0F2))
    )

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = Color(0xFF1F3D4D),
            secondary = Color(0xFFCF8C4B),
            background = Color(0xFFF4F1EA),
            surface = Color(0xFFFFFFFF)
        ),
        typography = MaterialTheme.typography.copy(
            headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold)
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().background(backgroundBrush)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopNav(
                        active = activeScreen,
                        onSelect = { activeScreen = it }
                    )
                    Column(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (activeScreen) {
                            Screen.Dashboard -> {
                                DashboardScreen(
                                    records = records,
                                    gradleRecords = gradleRecords,
                                    error = error,
                                    projectIds = projectIds,
                                    selectedProjectId = selectedProjectId,
                                    onProjectIdChange = { selectedProjectId = it.trim() },
                                    gitHashInput = gitHashInput,
                                    gitDateInput = gitDateInput,
                                    linesOfCodeInput = linesOfCodeInput,
                                    isEditing = isEditing,
                                    onGitHashChange = { gitHashInput = it },
                                    onGitDateChange = { gitDateInput = it },
                                    onLinesOfCodeChange = { linesOfCodeInput = it },
                                    onSubmit = {
                                        scope.launch {
                                            error = null
                                            val trimmedProjectId = selectedProjectId.trim()
                                            val trimmedHash = gitHashInput.trim()
                                            val trimmedDate = gitDateInput.trim()
                                            val countValue = linesOfCodeInput.trim().toIntOrNull()
                                            if (trimmedProjectId.isEmpty()) {
                                                error = "Select a project"
                                                return@launch
                                            }
                                            if (trimmedHash.isEmpty() || trimmedDate.isEmpty() || countValue == null) {
                                                error = "Enter git hash, git date, and lines of code"
                                                return@launch
                                            }
                                            try {
                                                if (isEditing) {
                                                    client.put("/counts/$trimmedHash") {
                                                        contentType(ContentType.Application.Json)
                                                        setBody(UpdateCountRequest(trimmedProjectId, trimmedDate, countValue))
                                                    }
                                                } else {
                                                    client.post("/counts") {
                                                        contentType(ContentType.Application.Json)
                                                        setBody(
                                                            CreateCountRequest(
                                                                trimmedProjectId,
                                                                trimmedHash,
                                                                trimmedDate,
                                                                countValue
                                                            )
                                                        )
                                                    }
                                                }
                                                reloadProjects()
                                                reloadRecords()
                                                resetForm()
                                            } catch (ex: Throwable) {
                                                error = ex.message ?: "Failed to submit"
                                            }
                                        }
                                    },
                                    onClear = { resetForm() },
                                    onEdit = { record ->
                                        gitHashInput = record.gitHash
                                        gitDateInput = record.gitDate.toString()
                                        linesOfCodeInput = record.linesOfCode.toString()
                                        isEditing = true
                                    },
                                    onDelete = { record ->
                                        scope.launch {
                                            error = null
                                            try {
                                                client.delete("/counts/${record.gitHash}") {
                                                    url { parameters.append("projectId", selectedProjectId) }
                                                }
                                                reloadProjects()
                                                reloadRecords()
                                                if (isEditing && gitHashInput == record.gitHash) {
                                                    resetForm()
                                                }
                                            } catch (ex: Throwable) {
                                                error = ex.message ?: "Failed to delete"
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
    }
}

private enum class Screen(val label: String) {
    Dashboard("Dashboard"),
    Settings("Settings")
}

@Composable
private fun TopNav(active: Screen, onSelect: (Screen) -> Unit) {
    Surface(color = Color(0xFF1F3D4D)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "CodebaseObserver ${BuildConfig.VERSION}",
                color = Color(0xFFF5F2EA),
                style = MaterialTheme.typography.titleLarge
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Screen.entries.forEach { screen ->
                    val selected = screen == active
                    if (selected) {
                        Button(onClick = { onSelect(screen) }) {
                            Text(screen.label)
                        }
                    } else {
                        TextButton(onClick = { onSelect(screen) }) {
                            Text(screen.label, color = Color(0xFFF5F2EA))
                        }
                    }
                }
            }
        }
    }
}