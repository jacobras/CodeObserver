package nl.jacobras.codebaseobserver.dashboard.detekt

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.WebElementView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gabrieldrn.carbon.Carbon
import io.ktor.client.HttpClient
import kotlinx.browser.document
import nl.jacobras.codebaseobserver.util.ui.loading.ProgressIndicator
import org.w3c.dom.HTMLIFrameElement

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun DetektReport(
    client: HttpClient,
    projectId: String
) {
    val viewModel = viewModel { DetektTrendsViewModel(client) }
    val reports by viewModel.reports.collectAsState(emptyList())
    val isLoading by viewModel.isLoading.collectAsState(false)
    val loadingError by viewModel.loadingError.collectAsState("")

    LaunchedEffect(projectId) {
        viewModel.setProjectId(projectId)
    }

    if (isLoading || loadingError.isNotEmpty()) {
        ProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            loading = isLoading,
            error = loadingError,
            onRetry = if (loadingError.isNotEmpty()) {
                { viewModel.refresh() }
            } else {
                null
            }
        )
        return
    }

    val latestReport = reports.maxByOrNull { it.gitDate }

    if (latestReport == null) {
        BasicText(
            modifier = Modifier.fillMaxWidth(),
            text = "No report available",
            style = Carbon.typography.body02
        )
        return
    }

    WebElementView(
        factory = {
            (document.createElement("iframe") as HTMLIFrameElement)
                .apply {
                    srcdoc = latestReport.htmlReport
                    sandbox.value = ""
                    frameBorder = "0"
                }
        },
        modifier = Modifier.fillMaxSize(),
        update = { iframe -> iframe.srcdoc = latestReport.htmlReport }
    )
}