package nl.jacobras.codebaseobserver.ui.loading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.button.Button
import com.gabrieldrn.carbon.button.ButtonSize
import com.gabrieldrn.carbon.loading.SmallLoading

/**
 * @param loading Shows a loading indicator.
 * @param error Shows an error message.
 * @param onRetry Shows a retry button if not `null`.
 */
@Composable
internal fun ProgressIndicator(
    loading: Boolean = true,
    error: String = "",
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (loading) {
        SmallLoading(modifier)
        return
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (error.isNotBlank()) {
            BasicText(
                modifier = Modifier.fillMaxWidth(),
                text = error,
                style = Carbon.typography.body02.copy(color = Carbon.theme.supportError)
            )
        }
        if (onRetry != null) {
            Button(
                label = "Retry",
                onClick = { onRetry() },
                buttonSize = ButtonSize.Medium
            )
        }
    }
}