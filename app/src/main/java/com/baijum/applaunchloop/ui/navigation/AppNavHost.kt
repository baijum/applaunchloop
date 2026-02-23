package com.baijum.applaunchloop.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.baijum.applaunchloop.data.repository.CampaignRepository
import com.baijum.applaunchloop.ui.screen.chooser.RoleChooserScreen
import com.baijum.applaunchloop.ui.screen.creator.CreatorDashboardScreen
import com.baijum.applaunchloop.ui.screen.onboarding.OnboardingScreen
import com.baijum.applaunchloop.ui.screen.tester.TesterDashboardScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(
    navController: NavHostController,
    campaignRepository: CampaignRepository,
    deviceId: String,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    fun navigateToDashboard(dashboard: String) {
        scope.launch { campaignRepository.setLastDashboard(dashboard) }
        val route = if (dashboard == "creator") {
            AppDestination.CreatorDashboard.route
        } else {
            AppDestination.TesterDashboard.route
        }
        navController.navigate(route) {
            popUpTo(0) { inclusive = true }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(AppDestination.RoleChooser.route) {
            RoleChooserScreen(
                onSelectCreator = { navigateToDashboard("creator") },
                onSelectTester = { navigateToDashboard("tester") }
            )
        }

        composable(AppDestination.CreatorDashboard.route) {
            CreatorDashboardScreen(
                campaignRepository = campaignRepository,
                deviceId = deviceId,
                onSwitchRole = { navigateToDashboard("tester") }
            )
        }

        composable(AppDestination.TesterDashboard.route) {
            TesterDashboardScreen(
                campaignRepository = campaignRepository,
                onSwitchRole = { navigateToDashboard("creator") }
            )
        }

        composable(
            route = AppDestination.Onboarding.route,
            arguments = listOf(navArgument("campaignId") { type = NavType.StringType })
        ) { backStackEntry ->
            val campaignId = backStackEntry.arguments?.getString("campaignId") ?: ""
            OnboardingScreen(
                campaignId = campaignId,
                campaignRepository = campaignRepository,
                onOnboardingComplete = {
                    scope.launch { campaignRepository.setLastDashboard("tester") }
                    navController.navigate(AppDestination.TesterDashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
