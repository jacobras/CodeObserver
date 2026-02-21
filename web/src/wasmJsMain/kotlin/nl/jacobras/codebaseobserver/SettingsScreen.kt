package nl.jacobras.codebaseobserver

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.Carbon

@Composable
internal fun SettingsScreen() {
    Column {
        BasicText(
            text = "Settings",
            style = Carbon.typography.heading06
        )
        Spacer(Modifier.height(16.dp))
        BasicText(
            text = "There are no settings to configure yet.",
            style = Carbon.typography.body02
        )
    }
}