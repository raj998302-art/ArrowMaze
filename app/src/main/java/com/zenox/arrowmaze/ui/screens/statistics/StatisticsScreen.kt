package com.zenox.arrowmaze.ui.screens.statistics

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    navController: NavController,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler { navController.popBackStack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                StatRow(icon = Icons.Outlined.SportsEsports, label = "Levels Completed", value = "${state.levelsCompleted}")
                StatRow(icon = Icons.Outlined.TouchApp, label = "Total Moves", value = "${state.totalMoves}")
                StatRow(icon = Icons.Outlined.Grade, label = "Perfect Levels", value = "${state.perfectLevels}")
                StatRow(icon = Icons.Outlined.Lightbulb, label = "Hints Used", value = "${state.hintsUsed}")
                StatRow(icon = Icons.Outlined.Timer, label = "Play Time", value = formatDuration(state.playTimeMs))
                StatRow(icon = Icons.Outlined.MonetizationOn, label = "Total Coins Earned", value = "${state.totalCoinsEarned}")
                StatRow(icon = Icons.Outlined.LocalFireDepartment, label = "Best Streak", value = "${state.bestStreak}")
                StatRow(icon = Icons.Outlined.Whatshot, label = "Current Streak", value = "${state.currentStreak}")
                StatRow(icon = Icons.Outlined.Close, label = "Wrong Taps", value = "${state.wrongTaps}")
                StatRow(icon = Icons.Outlined.CalendarToday, label = "Daily Challenges", value = "${state.dailyChallengesCompleted}")
            }
        }
    }
}

@Composable
private fun StatRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Text(value, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
    }
}

private fun formatDuration(ms: Long): String {
    if (ms <= 0) return "0m"
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}