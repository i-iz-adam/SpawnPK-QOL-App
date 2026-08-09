package com.spk.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.spk.app.ui.nav.Routes
import com.spk.app.ui.screens.*
import com.spk.app.ui.theme.*

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            SpawnPkTheme {
                SpawnPkAppRoot()
            }
        }
    }
}

private data class BottomTab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val bottomTabs = listOf(
    BottomTab(Routes.SEARCH, "Search", Icons.Filled.Search),
    BottomTab(Routes.STATS, "Stats", Icons.Filled.BarChart),
    BottomTab(Routes.PROFILE, "Profile", Icons.Filled.Person),
)

@Composable
private fun SpawnPkAppRoot() {
    val navController = rememberNavController()
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen(onReady = { showSplash = false })
        return
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination
    val showBottomBar = bottomTabs.any { currentRoute?.hierarchy?.any { d -> d.route == it.route } == true }

    Scaffold(
        containerColor = BgDeep,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = BgSurface, tonalElevation = 0.dp) {
                    bottomTabs.forEach { tab ->
                        val selected = currentRoute?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AccentMint,
                                selectedTextColor = AccentMint,
                                unselectedIconColor = TextTertiary,
                                unselectedTextColor = TextTertiary,
                                indicatorColor = BgSurfaceElevated
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).background(BgDeep)) {
            NavHost(navController = navController, startDestination = Routes.SEARCH) {
                composable(Routes.SEARCH) {
                    SearchScreen(onItemClick = { item -> navController.navigate(Routes.itemDetail(item.name)) })
                }
                composable(Routes.STATS) {
                    StatsScreen()
                }
                composable(Routes.PROFILE) {
                    ProfileScreen(
                        onItemClick = { name -> navController.navigate(Routes.itemDetail(name)) },
                        onNavigateFaq = { navController.navigate(Routes.FAQ) },
                        onNavigateDonate = { navController.navigate(Routes.DONATE) }
                    )
                }
                composable(Routes.FAQ) {
                    FaqScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.DONATE) {
                    DonateScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.ITEM_DETAIL) { backStackEntry2 ->
                    val encoded = backStackEntry2.arguments?.getString("itemName").orEmpty()
                    val itemName = java.net.URLDecoder.decode(encoded, "UTF-8")
                    ItemDetailScreen(itemName = itemName, onBack = { navController.popBackStack() })
                }
            }
        }
    }
}