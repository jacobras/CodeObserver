package nl.jacobras.codeobserver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.api.ExperimentalCarbonApi
import com.gabrieldrn.carbon.button.Button
import com.gabrieldrn.carbon.button.ButtonSize
import com.gabrieldrn.carbon.button.ButtonType
import com.gabrieldrn.carbon.foundation.color.LocalCarbonTheme
import com.gabrieldrn.carbon.foundation.spacing.SpacingScale
import com.gabrieldrn.carbon.notification.ToastNotification
import kotlinx.coroutines.launch
import nl.jacobras.codeobserver.auth.AuthState
import nl.jacobras.codeobserver.auth.LoginScreen
import nl.jacobras.codeobserver.dashboard.DashboardScreen
import nl.jacobras.codeobserver.di.RepositoryLocator
import nl.jacobras.codeobserver.di.UseCaseLocator
import nl.jacobras.codeobserver.dto.UserDto
import nl.jacobras.codeobserver.dto.UserRole
import nl.jacobras.codeobserver.settings.SettingsScreen
import nl.jacobras.codeobserver.users.UsersScreen
import nl.jacobras.codeobserver.util.ui.notification.Notifier
import nl.jacobras.codeobserver.util.ui.progress.ProgressIndicator
import nl.jacobras.codeobserver.util.ui.theme.COTheme
import nl.jacobras.codeobserver.web.BuildConfig

@Composable
fun App(
    onNavHostReady: suspend (NavController) -> Unit
) {
    val authState by RepositoryLocator.authRepository.authState.collectAsState()

    COTheme {
        when (val state = authState) {
            AuthState.Pending -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Carbon.theme.background),
                    contentAlignment = Alignment.Center
                ) {
                    ProgressIndicator(loading = true)
                }
            }

            AuthState.LoggedOut -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Carbon.theme.background)
                ) {
                    LoginScreen()
                }
            }

            is AuthState.LoggedIn -> {
                LaunchedEffect(state) {
                    RepositoryLocator.projectRepository.refresh()
                }
                MainContent(
                    user = state.user,
                    onNavHostReady = onNavHostReady
                )
            }
        }
    }
}

@OptIn(ExperimentalCarbonApi::class)
@Composable
private fun MainContent(
    user: UserDto,
    onNavHostReady: suspend (NavController) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val activeDestination =
        navBackStackEntry?.destination?.route?.let { Destination.fromRoute(it) } ?: Destination.Dashboard
    val notifications by Notifier.notifications.collectAsState(emptyList())
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Carbon.theme.background)
    ) {
        TopNav(
            active = activeDestination,
            user = user,
            onSelect = { navController.navigate(it.route) },
            onLogout = {
                scope.launch {
                    UseCaseLocator.logoutUseCase()
                }
            }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            NavHost(navController = navController, startDestination = Destination.Dashboard.route) {
                composable(Destination.Dashboard.route) { DashboardScreen() }
                composable(Destination.Settings.route) { SettingsScreen() }
                composable(Destination.Users.route) { UsersScreen() }
            }

            Column(Modifier.align(Alignment.TopEnd).verticalScroll(rememberScrollState())) {
                for (notification in notifications.sortedByDescending { it.time }) {
                    ToastNotification(
                        title = notification.title,
                        body = notification.message,
                        status = notification.status,
                        onClose = { Notifier.dismiss(notification.id) },
                        modifier = Modifier.width(400.dp)
                    )
                    Spacer(Modifier.height(SpacingScale.spacing03))
                }
            }
        }
    }

    LaunchedEffect(navController) {
        onNavHostReady(navController)
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun TopNav(
    active: Destination,
    user: UserDto,
    onSelect: (Destination) -> Unit,
    onLogout: () -> Unit
) {
    val windowSize = calculateWindowSizeClass()
    val wideNavBar = windowSize.widthSizeClass > WindowWidthSizeClass.Compact
    val tallScreen = windowSize.heightSizeClass > WindowHeightSizeClass.Compact
    val verticalPadding = if (tallScreen) 16.dp else 8.dp

    if (wideNavBar) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1F3D4D))
                .padding(horizontal = 24.dp, vertical = verticalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTitle()
            Spacer(Modifier.width(8.dp))
            Username(user.username, user.role)
            Spacer(Modifier.width(8.dp))
            Spacer(Modifier.weight(1f))
            MenuOptions(active = active, user = user, onSelect = onSelect, onLogout = onLogout)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1F3D4D))
                .padding(horizontal = 24.dp, vertical = verticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavTitle()
                Spacer(Modifier.width(8.dp))
                Username(user.username, user.role)
            }
            Spacer(Modifier.height(8.dp))
            MenuOptions(active = active, user = user, onSelect = onSelect, onLogout = onLogout)
        }
    }
}

@Composable
private fun NavTitle(modifier: Modifier = Modifier) {
    val demoSuffix = if (BuildConfig.IS_DEMO) {
        " (DEMO)"
    } else {
        ""
    }
    BasicText(
        modifier = modifier,
        text = "CodeObserver ${BuildConfig.VERSION}$demoSuffix",
        style = Carbon.typography.headingCompact02.copy(color = Color(0xFFF5F2EA))
    )
}

@Composable
private fun Username(username: String, role: UserRole, modifier: Modifier = Modifier) {
    BasicText(
        modifier = modifier,
        text = "${role.name.lowercase().capitalize(Locale.current)} ($username)",
        style = Carbon.typography.bodyCompact01.copy(color = Color(0xFFF5F2EA))
    )
}

@Composable
private fun MenuOptions(
    active: Destination,
    user: UserDto,
    onSelect: (Destination) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(
        LocalCarbonTheme provides LocalCarbonTheme.current.copy(
            buttonColors = LocalCarbonTheme.current.buttonColors.copy(
                buttonPrimary = Color(0xFF3D7999)
            ),
            linkPrimary = Color(0xFF86B5CE),
            linkPrimaryHover = Color(0xFF6599B8)
        )
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val visibleDestinations = Destination.entries.filter { screen ->
                screen != Destination.Users || user.role == UserRole.ADMIN
            }
            visibleDestinations.forEach { screen ->
                val selected = screen == active
                Button(
                    label = screen.label,
                    buttonType = if (selected) ButtonType.Primary else ButtonType.Ghost,
                    buttonSize = ButtonSize.Small,
                    onClick = { onSelect(screen) }
                )
            }
            Button(
                label = "Log out",
                buttonType = ButtonType.Ghost,
                buttonSize = ButtonSize.Small,
                onClick = onLogout
            )
        }
    }
}