package nl.jacobras.codeobserver.dashboard.detekt

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.tab.TabItem
import com.gabrieldrn.carbon.tab.TabList
import nl.jacobras.codeobserver.util.ui.chart.TimeView

@Composable
internal fun Detekt(
    timeView: TimeView,
    onSelectTimeView: (TimeView) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        val trendsTab = TabItem("Trends")
        val reportTab = TabItem("Latest report")
        val tabs = listOf(trendsTab, reportTab)
        var selectedTab by remember { mutableStateOf(trendsTab) }

        TabList(
            tabs = tabs,
            selectedTab = selectedTab,
            onTabSelected = { tab ->
                selectedTab = tab
            }
        )
        Spacer(Modifier.height(16.dp))

        if (selectedTab == trendsTab) {
            DetektTrends(timeView = timeView, onSelectTimeView = onSelectTimeView)
        } else {
            DetektReport()
        }
    }
}