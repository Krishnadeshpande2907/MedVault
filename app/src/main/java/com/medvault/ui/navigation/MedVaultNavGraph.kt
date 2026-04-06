package com.medvault.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.medvault.ui.screens.addvisit.AddEditVisitScreen
import com.medvault.ui.screens.export.ExportScreen
import com.medvault.ui.screens.lock.LockScreen
import com.medvault.ui.screens.mediaviewer.MediaViewerScreen
import com.medvault.ui.screens.settings.SettingsScreen
import com.medvault.ui.screens.timeline.TimelineScreen
import com.medvault.ui.screens.visitdetail.VisitDetailScreen

@Composable
fun MedVaultNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Screens that show the bottom nav bar
    val showBottomBar = currentRoute in listOf(
        Screen.Timeline.route,
        Screen.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Timeline.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // ── Tab screens ──────────────────────────────────────
            composable(Screen.Timeline.route) {
                TimelineScreen(
                    onAddVisit = {
                        navController.navigate(Screen.AddEditVisit.createRoute())
                    },
                    onVisitClick = { visitId ->
                        navController.navigate(Screen.VisitDetail.createRoute(visitId))
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            // ── Add / Edit Visit ─────────────────────────────────
            composable(
                route = "add_edit_visit?visitId={visitId}",
                arguments = listOf(
                    navArgument("visitId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                AddEditVisitScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            // ── Visit Detail ─────────────────────────────────────
            composable(
                route = "visit_detail/{visitId}",
                arguments = listOf(
                    navArgument("visitId") { type = NavType.StringType }
                )
            ) {
                VisitDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onEditVisit = { visitId ->
                        navController.navigate(Screen.AddEditVisit.createRoute(visitId))
                    },
                    onExport = { visitId ->
                        navController.navigate(Screen.Export.createRoute(visitId))
                    },
                    onMediaClick = { visitId, index ->
                        navController.navigate(Screen.MediaViewer.createRoute(visitId, index))
                    }
                )
            }

            // ── Media Viewer ─────────────────────────────────────
            composable(
                route = "media_viewer/{visitId}?startIndex={startIndex}",
                arguments = listOf(
                    navArgument("visitId") { type = NavType.StringType },
                    navArgument("startIndex") {
                        type = NavType.IntType
                        defaultValue = 0
                    }
                )
            ) {
                MediaViewerScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // ── Export ────────────────────────────────────────────
            composable(
                route = "export/{visitId}",
                arguments = listOf(
                    navArgument("visitId") { type = NavType.StringType }
                )
            ) {
                ExportScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // ── Lock Screen ──────────────────────────────────────
            composable(Screen.Lock.route) {
                LockScreen(
                    onUnlocked = {
                        navController.navigate(Screen.Timeline.route) {
                            popUpTo(Screen.Lock.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
