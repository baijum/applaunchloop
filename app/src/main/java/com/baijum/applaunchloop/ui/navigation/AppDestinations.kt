package com.baijum.applaunchloop.ui.navigation

sealed class AppDestination(val route: String) {
    data object RoleChooser : AppDestination("chooser")
    data object CreatorDashboard : AppDestination("creator")
    data object TesterDashboard : AppDestination("tester")
    data object Onboarding : AppDestination("onboarding/{campaignId}") {
        fun createRoute(campaignId: String) = "onboarding/$campaignId"
    }
}
