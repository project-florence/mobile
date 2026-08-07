package com.florence.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.florence.app.R
import com.florence.app.data.repository.AuthRepository
import com.florence.app.presentation.auth.LoginScreen
import com.florence.app.presentation.auth.RegisterScreen
import com.florence.app.presentation.home.ComingSoonScreen
import com.florence.app.presentation.home.DashboardScreen
import com.florence.app.presentation.profile.ProfileScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"
}

@HiltViewModel
class RootViewModel @Inject constructor(repo: AuthRepository) : ViewModel() {
    val session: StateFlow<Boolean> = repo.session
}

private enum class MainTab(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
) {
    Market("dashboard", R.string.nav_market, Icons.Filled.Home),
    Search("search", R.string.nav_search, Icons.Filled.Search),
    Portfolio("portfolio", R.string.nav_portfolio, Icons.Filled.Star),
    Profile("profile", R.string.nav_profile, Icons.Filled.Person),
}

/**
 * Kök navigasyon: oturum durumuna göre login/register ↔ ana uygulama.
 * Oturum değişimi (giriş/çıkış) burada tek noktadan yönetilir; backstack sıfırlanır.
 */
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
            MainScaffold(
                onGoToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
    }
}

@Composable
private fun MainScaffold(onGoToLogin: () -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(stringResource(tab.labelRes)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainTab.Market.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(MainTab.Market.route) { DashboardScreen() }
            composable(MainTab.Search.route) { ComingSoonScreen() }
            composable(MainTab.Portfolio.route) { ComingSoonScreen() }
            composable(MainTab.Profile.route) { ProfileScreen(onGoToLogin) }
        }
    }
}
