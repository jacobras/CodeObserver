package nl.jacobras.codebaseobserver

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
internal fun SettingsScreen() {
    Text("Settings", style = MaterialTheme.typography.headlineLarge)
    Text("There are no settings to configure yet.", style = MaterialTheme.typography.bodyLarge)
}