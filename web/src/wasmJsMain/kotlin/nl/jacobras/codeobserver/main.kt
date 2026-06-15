package nl.jacobras.codeobserver

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.bindToBrowserNavigation
import nl.jacobras.codeobserver.di.appModule
import nl.jacobras.codeobserver.web.BuildConfig
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

@OptIn(ExperimentalComposeUiApi::class, ExperimentalBrowserHistoryApi::class)
fun main() {
    ComposeViewport {
        KoinApplication(
            configuration = koinConfiguration {
                modules(appModule(BuildConfig.IS_DEMO))
            }
        ) {
            App(
                onNavHostReady = { it.bindToBrowserNavigation() }
            )
        }
    }
}