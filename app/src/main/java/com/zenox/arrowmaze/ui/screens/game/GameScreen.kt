package com.zenox.arrowmaze.ui.screens.game

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.zenox.arrowmaze.ui.screens.game.components.GameBoard
import com.zenox.arrowmaze.ui.screens.game.components.GameHUD
import com.zenox.arrowmaze.ui.screens.game.components.GameOverDialog
import com.zenox.arrowmaze.ui.screens.game.components.LevelCompleteDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    level: Int,
    worldSalt: Int,
    navController: NavController,
    viewModel: GameViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        navController.popBackStack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top HUD
                GameHUD(
                    level = state.puzzle?.level ?: level,
                    difficultyName = state.puzzle?.difficulty?.label ?: "",
                    coins = 0,
                    hearts = state.gameState?.hearts ?: 0,
                    maxHearts = state.gameState?.maxHearts ?: 3,
                    onBack = { navController.popBackStack() }
                )

                // Game Board
                val puzzle = state.puzzle
                if (puzzle != null) {
                    GameBoard(
                        puzzle = puzzle,
                        gameState = state.gameState,
                        removingSnakeId = state.removingSnakeId,
                        wrongSnakeId = state.wrongSnakeId,
                        currentTheme = state.currentTheme,
                        currentSkin = state.currentSkin,
                        currentTrail = state.currentTrail,
                        onCellTap = { row, col -> viewModel.onCellTap(row, col) }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Bar
                Surface(
                    shadowElevation = 8.dp,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hint button
                        FilledTonalButton(
                            onClick = { viewModel.useHint() },
                            enabled = (state.gameState?.hintsRemaining ?: 0) > 0 && !state.gameState!!.isComplete
                        ) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hint (${state.gameState?.hintsRemaining ?: 0})")
                        }

                        // Restart button
                        IconButton(onClick = { viewModel.restartLevel() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Restart")
                        }

                        // Home button
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.Home, contentDescription = "Home")
                        }
                    }
                }
            }
        }

        // Game Over overlay
        if (state.showGameOver) {
            GameOverDialog(
                onWatchAd = { viewModel.watchAdForHearts() },
                onRestart = { viewModel.restartLevel() },
                onHome = { navController.popBackStack() }
            )
        }

        // Level Complete overlay
        if (state.showLevelComplete) {
            LevelCompleteDialog(
                coinsEarned = state.earnedCoins,
                isPerfect = state.isPerfect,
                isChestLevel = state.isChestLevel,
                isMilestone = state.isMilestone,
                onNext = { viewModel.nextLevel() },
                onHome = { navController.popBackStack() }
            )
        }
    }
}
