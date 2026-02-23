package com.baijum.applaunchloop.ui.screen.tester

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
fun TesterDashboardScreen(
    campaignRepository: CampaignRepository,
    onSwitchRole: () -> Unit,
    testerViewModel: TesterViewModel = viewModel(
        factory = TesterViewModel.Factory(campaignRepository)
    )
) {
    val uiState by testerViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val progress by animateFloatAsState(
        targetValue = uiState.streakCount / 14f,
        animationSpec = tween(durationMillis = 600),
        label = "progress"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tester Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    TextButton(onClick = onSwitchRole) {
                        Text("Switch to Creator")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!uiState.hasActiveCampaign) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "No Active Campaign",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Open a campaign join link to get started, or ask a creator to share one with you.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
            } else {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (uiState.isComplete) "Testing Complete!" else "14-Day Testing Progress",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${uiState.streakCount} / 14",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.isComplete)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "days completed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp),
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Target App",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = uiState.targetPackageName.ifBlank { "Not set" },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { launchTargetApp(context, uiState.targetPackageName) },
                    enabled = uiState.targetPackageName.isNotBlank() && !uiState.isComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (uiState.isComplete) "All Done!" else "Launch App",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (uiState.isComplete) {
                    Text(
                        text = "Congratulations! You've completed the 14-day testing period.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun launchTargetApp(context: Context, packageName: String) {
    val intent: Intent? = context.packageManager.getLaunchIntentForPackage(packageName)
    if (intent != null) {
        context.startActivity(intent)
    } else {
        Toast.makeText(context, "App not installed: $packageName", Toast.LENGTH_LONG).show()
    }
}
