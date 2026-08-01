package com.zenox.arrowmaze.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zenox.arrowmaze.data.repository.AuthRepository
import com.zenox.arrowmaze.ui.screens.achievements.AchievementsScreen
import com.zenox.arrowmaze.ui.screens.dailychallenge.DailyChallengeScreen
import com.zenox.arrowmaze.ui.screens.friends.FriendsScreen
import com.zenox.arrowmaze.ui.screens.game.GameScreen
import com.zenox.arrowmaze.ui.screens.home.HomeScreen
import com.zenox.arrowmaze.ui.screens.leaderboard.LeaderboardScreen
import com.zenox.arrowmaze.ui.screens.practice.PracticeScreen
import com.zenox.arrowmaze.ui.screens.profile.ProfileScreen
import com.zenox.arrowmaze.ui.screens.settings.SettingsScreen
import com.zenox.arrowmaze.ui.screens.shop.ShopScreen
import com.zenox.arrowmaze.ui.screens.statistics.StatisticsScreen

@Composable
fun AppNavigation(
    authRepository: AuthRepository,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavRoutes.HOME
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(NavRoutes.HOME) {
            HomeScreen(navController = navController)
        }

        composable(
            route = NavRoutes.GAME + "/{level}/{worldSalt}",
            arguments = listOf(
                navArgument("level") { type = NavType.IntType },
                navArgument("worldSalt") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val level = backStackEntry.arguments?.getInt("level") ?: 1
            val worldSalt = backStackEntry.arguments?.getInt("worldSalt") ?: 0
            GameScreen(
                level = level,
                worldSalt = worldSalt,
                navController = navController
            )
        }

        composable(NavRoutes.PRACTICE) {
            PracticeScreen(navController = navController)
        }

        composable(NavRoutes.DAILY_CHALLENGE) {
            DailyChallengeScreen(navController = navController)
        }

        composable(NavRoutes.PROFILE) {
            ProfileScreen(navController = navController)
        }

        composable(NavRoutes.SHOP) {
            ShopScreen(navController = navController)
        }

        composable(NavRoutes.ACHIEVEMENTS) {
            AchievementsScreen(navController = navController)
        }

        composable(NavRoutes.LEADERBOARD) {
            LeaderboardScreen(navController = navController)
        }

        composable(NavRoutes.STATISTICS) {
            StatisticsScreen(navController = navController)
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(navController = navController)
        }

        composable(NavRoutes.FRIENDS) {
            FriendsScreen(navController = navController)
        }
    }
}
