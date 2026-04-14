package nl.jacobras.codeobserver.util.ui.button

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.button.Button
import com.gabrieldrn.carbon.button.ButtonSize
import com.gabrieldrn.carbon.button.ButtonType
import com.gabrieldrn.carbon.foundation.color.LocalCarbonTheme
import com.gabrieldrn.carbon.foundation.spacing.SpacingScale
import com.gabrieldrn.carbon.loading.SmallLoading

@Composable
fun SmallProgressButton(
    label: String,
    loading: Boolean,
    buttonType: ButtonType,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    if (loading) {
        CompositionLocalProvider(
            LocalCarbonTheme provides LocalCarbonTheme.current.copy(
                interactive = when (buttonType) {
                    ButtonType.PrimaryDanger,
                    ButtonType.GhostDanger,
                    ButtonType.TertiaryDanger -> LocalCarbonTheme.current.buttonColors.buttonDangerHover
                    else -> LocalCarbonTheme.current.interactive
                }
            )
        ) {
            SmallLoading(
                modifier = Modifier.padding(
                    horizontal = SpacingScale.spacing05,
                    vertical = 7.dp // As in Carbon's ButtonSize.kt
                )
            )
        }
    } else {
        Button(
            label = label,
            buttonType = buttonType,
            buttonSize = ButtonSize.Small,
            isEnabled = isEnabled,
            onClick = onClick
        )
    }
}