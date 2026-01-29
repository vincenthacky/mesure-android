package com.example.mesure_android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mesure_android.ui.screen.ar.ArScreen
import com.example.mesure_android.ui.screen.scanner.ScannerScreen
import com.example.mesure_android.ui.screen.sessions.SessionListScreen

sealed class Screen(val route: String) {
    data object Scanner : Screen("scanner")
    data object Ar : Screen("ar/{siteId}/{sessionId}") {
        fun createRoute(siteId: String, sessionId: Long) = "ar/$siteId/$sessionId"
    }
    data object Sessions : Screen("sessions")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Scanner.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Scanner.route) {
            ScannerScreen(
                onNavigateToAr = { siteId, sessionId ->
                    navController.navigate(Screen.Ar.createRoute(siteId, sessionId)) {
                        popUpTo(Screen.Scanner.route) { inclusive = true }
                    }
                },
                onNavigateToSessions = {
                    navController.navigate(Screen.Sessions.route)
                }
            )
        }

        composable(
            route = Screen.Ar.route,
            arguments = listOf(
                navArgument("siteId") { type = NavType.StringType },
                navArgument("sessionId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val siteId = backStackEntry.arguments?.getString("siteId") ?: return@composable
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: return@composable
            ArScreen(
                siteId = siteId,
                sessionId = sessionId,
                onBack = {
                    navController.navigate(Screen.Scanner.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Sessions.route) {
            SessionListScreen(
                onBack = { navController.popBackStack() },
                onSessionSelected = { siteId, sessionId ->
                    navController.navigate(Screen.Ar.createRoute(siteId, sessionId)) {
                        popUpTo(Screen.Scanner.route)
                    }
                }
            )
        }
    }
}
