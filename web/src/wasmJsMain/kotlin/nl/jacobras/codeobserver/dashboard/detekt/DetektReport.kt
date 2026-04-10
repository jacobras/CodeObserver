package nl.jacobras.codeobserver.dashboard.detekt

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.WebElementView
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.browser.document
import nl.jacobras.codeobserver.di.RepositoryLocator
import nl.jacobras.codeobserver.util.data.RequestState
import nl.jacobras.codeobserver.util.ui.UiState
import nl.jacobras.codeobserver.util.ui.progress.EmptyState
import nl.jacobras.codeobserver.util.ui.progress.ProgressIndicator
import org.w3c.dom.HTMLIFrameElement

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun DetektReport() {
    val viewModel = viewModel {
        DetektTrendsViewModel(
            detektReportRepository = RepositoryLocator.detektReportRepository,
            projectRepository = RepositoryLocator.projectRepository
        )
    }
    val latestReport by viewModel.detailReport.collectAsState("")
    val state by viewModel.detailReportState.collectAsState(UiState())
    val projectId by viewModel.projectId.collectAsState()

    Column {
        when (val loading = state.loading) {
            is RequestState.Working -> {
                ProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    loading = true
                )
            }
            is RequestState.Error -> {
                ProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    error = loading.type.name,
                    onRetry = { viewModel.refresh() }
                )
            }
            RequestState.Idle -> Unit
        }

        if (latestReport.isEmpty()) {
            EmptyState(
                text = "No Detekt report found",
                command = "report-detekt --htmlFile=build/reports/detekt/detekt.html",
                projectId = projectId ?: return
            )
            return
        }

        WebElementView(
            factory = {
                (document.createElement("iframe") as HTMLIFrameElement)
                    .apply {
                        srcdoc = latestReport
                        sandbox.value = ""
                        frameBorder = "0"
                    }
            },
            modifier = Modifier.fillMaxSize(),
            update = { iframe -> iframe.srcdoc = latestReport }
        )
    }
}