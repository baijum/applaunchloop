package com.baijum.applaunchloop

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.baijum.applaunchloop.ui.navigation.AppDestination
import com.baijum.applaunchloop.ui.navigation.AppNavHost
import com.baijum.applaunchloop.ui.theme.AppLaunchLoopTheme
import com.baijum.applaunchloop.worker.WorkScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or not, WorkManager still runs; notification just won't show */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()

        val app = application as AppLaunchLoopApplication
        val deepLinkCampaignId = extractCampaignId(intent)
        @Suppress("HardwareIds")
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        WorkScheduler.scheduleDailyTestWork(this)

        setContent {
            AppLaunchLoopTheme {
                val navController = rememberNavController()

                val lastDashboard = runBlocking { app.campaignRepository.lastDashboard.first() }
                val startDestination = when {
                    deepLinkCampaignId != null -> AppDestination.Onboarding.createRoute(deepLinkCampaignId)
                    lastDashboard == "creator" -> AppDestination.CreatorDashboard.route
                    lastDashboard == "tester" -> AppDestination.TesterDashboard.route
                    else -> AppDestination.RoleChooser.route
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        campaignRepository = app.campaignRepository,
                        deviceId = deviceId,
                        startDestination = startDestination
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val campaignId = extractCampaignId(intent)
        if (campaignId != null) {
            recreate()
        }
    }

    private fun extractCampaignId(intent: Intent?): String? {
        val data = intent?.data ?: return null
        if (data.host == "baijum.github.io" && data.path?.startsWith("/applaunchloop/join/") == true) {
            return data.path?.removePrefix("/applaunchloop/join/")?.trimEnd('/')?.takeIf { it.isNotBlank() }
        }
        return null
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
