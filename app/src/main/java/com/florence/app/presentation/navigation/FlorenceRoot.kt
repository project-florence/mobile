package com.florence.app.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.florence.app.R
import com.florence.app.data.repository.AuthRepository
import com.florence.app.presentation.advisor.AdvisorScreen
import com.florence.app.presentation.admin.AdminScreen
import com.florence.app.presentation.auth.LoginScreen
import com.florence.app.presentation.auth.RegisterScreen
import com.florence.app.presentation.company.CompanyDetailScreen
import com.florence.app.presentation.economy.EconomyScreen
import com.florence.app.presentation.home.DashboardScreen
import com.florence.app.presentation.ipo.IpoScreen
import com.florence.app.presentation.portfolio.PortfolioScreen
import com.florence.app.presentation.profile.ProfileScreen
import com.florence.app.presentation.reports.ReportsScreen
import com.florence.app.presentation.search.SearchScreen
import com.florence.app.presentation.settings.CreditsViewModel
import com.florence.app.presentation.settings.SettingsScreen
import com.florence.app.presentation.watchlist.WatchlistScreen
import com.florence.app.presentation.components.LogoMark
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.viewModelScope

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"
}

@HiltViewModel
class RootViewModel @Inject constructor(
    private val repo: AuthRepository,
) : ViewModel() {
    val session: StateFlow<Boolean> = repo.session

    fun logout() {
        viewModelScope.launch { repo.logout() }
    }
}

/**
 * Web'in sol sidebar'ının mobil karşılığı: hamburger menü ile açılan çekmece.
 * Alttaki sekme çubuğu yok — web ile birebir gezinme modeli.
 */
private data class DrawerItem(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
)

private val DRAWER_MARKET_ITEMS = listOf(
    DrawerItem("dashboard", R.string.nav_market, Icons.Filled.Home),
    DrawerItem("search", R.string.nav_search, Icons.Filled.Search),
    DrawerItem("watchlist", R.string.nav_watchlist, Icons.Filled.Star),
    DrawerItem("portfolio", R.string.nav_portfolio, Icons.Filled.List),
    DrawerItem("reports", R.string.nav_reports, Icons.Filled.Create),
    DrawerItem("advisor", R.string.nav_advisor, Icons.Filled.Send),
    DrawerItem("ipos", R.string.nav_ipos, Icons.Filled.PlayArrow),
    DrawerItem("economy", R.string.nav_economy, Icons.Filled.Info),
)

private val DRAWER_ACCOUNT_ITEMS = listOf(
    DrawerItem("settings", R.string.nav_settings, Icons.Filled.Settings),
    DrawerItem("profile", R.string.nav_profile, Icons.Filled.Person),
)

private fun drawerTitleFor(route: String?): Int = when (route) {
    "dashboard" -> R.string.nav_market
    "search" -> R.string.nav_search
    "watchlist" -> R.string.nav_watchlist
    "portfolio" -> R.string.nav_portfolio
    "reports" -> R.string.nav_reports
    "advisor" -> R.string.nav_advisor
    "ipos" -> R.string.nav_ipos
    "economy" -> R.string.nav_economy
    "settings" -> R.string.nav_settings
    "admin" -> R.string.nav_admin
    "profile" -> R.string.nav_profile
    else -> R.string.app_name
}

@Composable
fun FlorenceRoot(viewModel: RootViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val session by viewModel.session.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(session) {
        navController.navigate(if (session) Routes.MAIN else Routes.LOGIN) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (session) Routes.MAIN else Routes.LOGIN,
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
            )
        }
        composable(Routes.MAIN) {
            MainScaffold(viewModel = viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold(viewModel: RootViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isDetail = currentRoute?.startsWith("company/") == true
    val creditsViewModel: CreditsViewModel = hiltViewModel()
    val creditsState by creditsViewModel.uiState.collectAsStateWithLifecycle()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.fillMaxHeight().width(300.dp)) {
                    Row(
                        modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LogoMark(size = 34.dp)
                        Spacer(Modifier.size(12.dp))
                        Column {
                            Text(
                                text = "Florence",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Yatırım Asistanı",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // Coin rozeti
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Coin",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.size(8.dp))
                        Text(
                            text = creditsState.credits?.let { "%.1f".format(it) } ?: "—",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "coin",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(Modifier.size(4.dp))
                    DrawerSectionLabel(stringResource(R.string.nav_section_market))
                    DRAWER_MARKET_ITEMS.forEach { item ->
                        DrawerNavItem(
                            item = item,
                            currentRoute = currentRoute,
                            onNavigate = {
                                scope.launch { drawerState.close() }
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    }
                    Spacer(Modifier.size(6.dp))
                    DrawerSectionLabel(stringResource(R.string.nav_section_account))
                    DRAWER_ACCOUNT_ITEMS.forEach { item ->
                        DrawerNavItem(
                            item = item,
                            currentRoute = currentRoute,
                            onNavigate = {
                                scope.launch { drawerState.close() }
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.nav_logout)) },
                        icon = { Icon(Icons.Filled.ExitToApp, contentDescription = null) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            // Oturum token'ları temizlenince Root, session flow ile login'e yönlendirir.
                            viewModel.logout()
                        },
                        modifier = Modifier.padding(horizontal = 10.dp),
                    )
                    Spacer(Modifier.size(12.dp))
                }
            }
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(stringResource(drawerTitleFor(currentRoute)))
                    },
                    navigationIcon = {
                        if (isDetail) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Menü")
                            }
                        }
                    },
                    actions = {
                        if (!isDetail) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(Modifier.size(4.dp))
                                Text(
                                    text = creditsState.credits?.let { "%.1f".format(it) } ?: "—",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "dashboard",
                modifier = Modifier.padding(innerPadding),
            ) {
                composable("dashboard") {
                    DashboardScreen(
                        onOpenCompany = { ticker ->
                            navController.navigate("company/$ticker")
                        },
                    )
                }
                composable("search") {
                    SearchScreen(
                        onOpenCompany = { ticker ->
                            navController.navigate("company/$ticker")
                        },
                    )
                }
                composable("watchlist") {
                    WatchlistScreen(
                        onOpenCompany = { ticker ->
                            navController.navigate("company/$ticker")
                        },
                    )
                }
                composable("portfolio") { PortfolioScreen() }
                composable("reports") { ReportsScreen() }
                composable("advisor") { AdvisorScreen() }
                composable("ipos") { IpoScreen() }
                composable("economy") { EconomyScreen() }
                composable("settings") { SettingsScreen() }
                composable("admin") { AdminScreen() }
                composable("profile") { ProfileScreen(onLoggedOut = {}) }
                composable(
                    route = "company/{ticker}",
                    arguments = listOf(navArgument("ticker") { type = NavType.StringType }),
                ) {
                    CompanyDetailScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}

@Composable
private fun DrawerSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 28.dp, top = 8.dp, bottom = 4.dp),
    )
}

@Composable
private fun DrawerNavItem(
    item: DrawerItem,
    currentRoute: String?,
    onNavigate: () -> Unit,
) {
    NavigationDrawerItem(
        label = { Text(stringResource(item.labelRes)) },
        icon = { Icon(item.icon, contentDescription = null) },
        selected = currentRoute == item.route,
        onClick = onNavigate,
        modifier = Modifier.padding(horizontal = 10.dp),
    )
}
