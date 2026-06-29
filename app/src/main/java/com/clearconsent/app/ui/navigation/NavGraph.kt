package com.clearconsent.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.clearconsent.app.ui.screens.dailydigest.DailyDigestScreen
import com.clearconsent.app.ui.screens.home.HomeScreen
import com.clearconsent.app.ui.screens.recording.RecordingScreen
import com.clearconsent.app.ui.screens.sessiondetail.SessionDetailScreen
import com.clearconsent.app.ui.screens.sessions.SessionsScreen
import com.clearconsent.app.ui.screens.settings.SettingsScreen
import com.clearconsent.app.ui.screens.transcript.TranscriptScreen

object Routes {
    const val HOME = "home"
    const val RECORDING = "recording"
    const val SESSIONS = "sessions"
    const val SESSION_DETAIL = "session_detail/{sessionId}"
    const val TRANSCRIPT = "transcript/{sessionId}"
    const val DAILY_DIGEST = "daily_digest"
    const val SETTINGS = "settings"

    fun sessionDetail(sessionId: String) = "session_detail/$sessionId"
    fun transcript(sessionId: String) = "transcript/$sessionId"
}

@Composable
fun ClearConsentNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                onStartRecording = { navController.navigate(Routes.RECORDING) },
                onViewSessions = { navController.navigate(Routes.SESSIONS) },
                onViewDigest = { navController.navigate(Routes.DAILY_DIGEST) },
                onSessionClick = { sessionId -> navController.navigate(Routes.sessionDetail(sessionId)) },
                onSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.RECORDING) {
            RecordingScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SESSIONS) {
            SessionsScreen(
                onSessionClick = { sessionId -> navController.navigate(Routes.sessionDetail(sessionId)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.SESSION_DETAIL,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: return@composable
            SessionDetailScreen(
                sessionId = sessionId,
                onViewTranscript = { navController.navigate(Routes.transcript(sessionId)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.TRANSCRIPT,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: return@composable
            TranscriptScreen(
                sessionId = sessionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.DAILY_DIGEST) {
            DailyDigestScreen(
                onSessionClick = { sessionId -> navController.navigate(Routes.sessionDetail(sessionId)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
