package nl.jacobras.codeobserver.dashboard.modulegraph

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.tab.TabItem
import com.gabrieldrn.carbon.tab.TabList
import nl.jacobras.codeobserver.util.ui.commandinfo.CommandInfoBox
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ModuleGraph() {
    val viewModel = koinViewModel<ModuleGraphViewModel>()
    val projectId by viewModel.projectId.collectAsState()

    Column(modifier = Modifier) {
        val tabs = listOf(
            TabItem("Metrics"),
            TabItem("Graph"),
            TabItem("Module rules"),
            TabItem("Module types")
        )
        var selectedTab by remember { mutableStateOf(tabs.first()) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TabList(
                tabs = tabs,
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                }
            )
            projectId?.let {
                Spacer(Modifier.weight(1f))
                CommandInfoBox(
                    command = "measure",
                    projectId = it
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        when (selectedTab.label) {
            "Metrics" -> GradleMetrics(viewModel)
            "Graph" -> Graph(viewModel)
            "Module rules" -> ModuleRules()
            "Module types" -> ModuleTypes()
        }
    }
}