package com.baijum.applaunchloop.ui.screen.onboarding

import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.baijum.applaunchloop.data.repository.CampaignRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    campaignId: String,
    campaignRepository: CampaignRepository,
    onOnboardingComplete: () -> Unit,
    onboardingViewModel: OnboardingViewModel = viewModel(
        factory = OnboardingViewModel.Factory(campaignId, campaignRepository)
    )
) {
    val uiState by onboardingViewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Join Campaign") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        LinearProgressIndicator(
                            progress = { (uiState.currentStep + 1) / uiState.totalSteps.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(uiState.totalSteps) { index ->
                                StepIndicator(
                                    stepNumber = index + 1,
                                    isActive = index == uiState.currentStep,
                                    isCompleted = index < uiState.currentStep
                                )
                                if (index < uiState.totalSteps - 1) {
                                    Spacer(modifier = Modifier.width(24.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        when (uiState.currentStep) {
                            0 -> StepContent(
                                title = "Step 1: Join Google Group",
                                description = "Join the testing Google Group to become an eligible tester.",
                                detail = uiState.googleGroupEmail
                            )
                            1 -> StepContent(
                                title = "Step 2: Opt In to Testing",
                                description = "Open the Google Play testing opt-in link to join the closed test track.",
                                detail = uiState.packageName
                            )
                            2 -> StepContent(
                                title = "Step 3: Download the App",
                                description = "Install the app from the Google Play Store. It may take a few minutes to appear after opting in.",
                                detail = uiState.packageName
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        when (uiState.currentStep) {
                            0 -> {
                                Button(
                                    onClick = {
                                        val groupUrl = "https://groups.google.com/g/${
                                            uiState.googleGroupEmail
                                                .substringBefore("@")
                                        }"
                                        val customTabsIntent = CustomTabsIntent.Builder().build()
                                        customTabsIntent.launchUrl(context, Uri.parse(groupUrl))
                                    },
                                    modifier = Modifier.fillMaxWidth().height(52.dp)
                                ) {
                                    Text("Open Google Group", fontSize = 16.sp)
                                }
                                OutlinedButton(
                                    onClick = onboardingViewModel::advanceStep,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("I've Joined, Next")
                                }
                            }
                            1 -> {
                                Button(
                                    onClick = {
                                        val optInUrl = "https://play.google.com/apps/testing/${uiState.packageName}"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(optInUrl))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.fillMaxWidth().height(52.dp)
                                ) {
                                    Text("Open Opt-In Page", fontSize = 16.sp)
                                }
                                OutlinedButton(
                                    onClick = onboardingViewModel::advanceStep,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("I've Opted In, Next")
                                }
                            }
                            2 -> {
                                Button(
                                    onClick = {
                                        val playStoreUri = Uri.parse(
                                            "market://details?id=${uiState.packageName}"
                                        )
                                        val intent = Intent(Intent.ACTION_VIEW, playStoreUri).apply {
                                            setPackage("com.android.vending")
                                        }
                                        if (intent.resolveActivity(context.packageManager) != null) {
                                            context.startActivity(intent)
                                        } else {
                                            val webUri = Uri.parse(
                                                "https://play.google.com/store/apps/details?id=${uiState.packageName}"
                                            )
                                            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(52.dp)
                                ) {
                                    Text("Open Play Store", fontSize = 16.sp)
                                }
                                Button(
                                    onClick = {
                                        onboardingViewModel.saveAndFinish(onOnboardingComplete)
                                    },
                                    modifier = Modifier.fillMaxWidth().height(52.dp)
                                ) {
                                    Text("Finish Setup", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(stepNumber: Int, isActive: Boolean, isCompleted: Boolean) {
    val backgroundColor = when {
        isActive -> MaterialTheme.colorScheme.primary
        isCompleted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val textColor = when {
        isActive || isCompleted -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = CircleShape,
        color = backgroundColor,
        modifier = Modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = if (isCompleted) "\u2713" else "$stepNumber",
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StepContent(title: String, description: String, detail: String) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (detail.isNotBlank()) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = detail,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
