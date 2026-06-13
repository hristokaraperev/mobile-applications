package com.calorietracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.calorietracker.ui.auth.LoginScreen
import com.calorietracker.ui.auth.RegisterScreen
import com.calorietracker.ui.diary.DiaryScreen

/**
 * Top-level navigation graph. Starts at Login; on successful authentication the
 * back stack is reset to the Diary destination so the user cannot navigate back
 * into the auth flow.
 */
@Composable
fun CalorieTrackerNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onAuthenticated = { navController.toDiary() },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onAuthenticated = { navController.toDiary() },
                onNavigateToLogin = { navController.popBackStack() },
            )
        }
        composable(Routes.DIARY) {
            DiaryScreen()
        }
    }
}

/** Navigates to Diary, clearing the auth flow from the back stack. */
private fun NavHostController.toDiary() {
    navigate(Routes.DIARY) {
        popUpTo(Routes.LOGIN) { inclusive = true }
        launchSingleTop = true
    }
}
