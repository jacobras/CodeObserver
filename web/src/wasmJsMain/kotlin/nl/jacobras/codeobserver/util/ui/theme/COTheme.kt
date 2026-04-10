package nl.jacobras.codeobserver.util.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.CarbonDesignSystem
import com.gabrieldrn.carbon.foundation.color.WhiteTheme
import com.patrykandpatrick.vico.compose.common.DefaultColors
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.VicoTheme
import com.patrykandpatrick.vico.compose.common.VicoTheme.CandlestickCartesianLayerColors

@Composable
internal fun COTheme(
    content: @Composable () -> Unit
) {
    CarbonDesignSystem(
        theme = WhiteTheme.copy(
            borderInteractive = Color(0xFF1F3D4D),
            layerSelectedInverse = Color(0xFF1F3D4D),
            buttonColors = WhiteTheme.buttonColors.copy(
                buttonPrimary = Color(0xFF3F739D),
                buttonPrimaryHover = Color(0xFF376D8A),
                buttonPrimaryActive = Color(0xFF2A5369),
                buttonTertiary = Color(0xFF3F739D),
                buttonTertiaryHover = Color(0xFF376D8A),
                buttonTertiaryActive = Color(0xFF2A5369)
            )
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
            content()
        }
    }
}