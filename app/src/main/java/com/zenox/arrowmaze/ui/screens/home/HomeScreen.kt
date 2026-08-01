package com.zenox.arrowmaze.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.zenox.arrowmaze.domain.model.GameDifficulty
import com.zenox.arrowmaze.ui.navigation.NavRoutes
import com.zenox.arrowmaze.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // Floaty animation for logo
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffset"
    )

    // Sheen animation for play button
    val sheenOffset by infiniteTransition.animateFloat(
        initialValue = -200f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheen"
    )

    LaunchedEffect(state.showNewGameDialog) {
        if (!state.showNewGameDialog) return@LaunchedEffect
        // Navigation triggered when dialog confirmed
    }

    // Navigate to game when play is pressed
    LaunchedEffect(Unit) { }

    // --- Daily Reward Dialog ---
    if (state.showDailyReward) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDailyReward() },
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    "Daily Reward!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Day ${state.dailyStreak}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // 30-day calendar grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(30) { day ->
                            val isClaimed = day < state.dailyStreak
                            val isCurrent = day == state.dailyStreak - 1
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when {
                                            isCurrent -> MaterialTheme.colorScheme.primary
                                            isClaimed -> MaterialTheme.colorScheme.tertiary
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${day + 1}",
                                    fontSize = 11.sp,
                                    color = when {
                                        isCurrent || isClaimed -> MaterialTheme.colorScheme.onPrimary
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = CoinGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "+${state.dailyRewardCoins}",
                                fontWeight = FontWeight.Bold,
                                color = CoinGold
                            )
                        }
                        if (state.dailyRewardHints > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = HintCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "+${state.dailyRewardHints}",
                                    fontWeight = FontWeight.Bold,
                                    color = HintCyan
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.claimDailyReward() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("Claim", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // --- Practice Mode Dialog ---
    if (state.showPracticeDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissPracticeDialog() },
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    "Practice Mode",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GameDifficulty.entries.forEach { difficulty ->
                        val diffColor = Color(difficulty.color)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.startPractice(difficulty)
                                    navController.navigate("${NavRoutes.GAME}/0/${difficulty.index}")
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = diffColor.copy(alpha = 0.12f)
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    difficulty.label,
                                    fontWeight = FontWeight.SemiBold,
                                    color = diffColor
                                )
                                Text(
                                    "${difficulty.baseCoins} coins",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // --- New Game Confirmation Dialog ---
    if (state.showNewGameDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissNewGameDialog() },
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text("Start Game", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Play Level ${state.level}?\nDifficulty: ${state.difficultyName}")
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.startGame()
                    navController.navigate("${NavRoutes.GAME}/${state.level}/${state.worldSalt}")
                }) {
                    Text("Play")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissNewGameDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("ArrowMaze", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { navController.navigate(NavRoutes.SETTINGS) }) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.navigate(NavRoutes.PROFILE) }) {
                        Icon(Icons.Outlined.Person, contentDescription = "Profile")
                    }
                    IconButton(onClick = { navController.navigate(NavRoutes.SHOP) }) {
                        Icon(Icons.Outlined.ShoppingCart, contentDescription = "Shop")
                    }
                    IconButton(onClick = { navController.navigate(NavRoutes.LEADERBOARD) }) {
                        Icon(Icons.Outlined.EmojiEvents, contentDescription = "Leaderboard")
                    }
                    IconButton(onClick = { navController.navigate(NavRoutes.FRIENDS) }) {
                        Icon(Icons.Outlined.Group, contentDescription = "Friends")
                    }
                }
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // --- Logo with floaty animation ---
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.offset(y = floatOffset.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        "ArrowMaze",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Level ${state.level} • ${state.difficultyName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- Stats chips row ---
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Coins
                    StatChip(
                        icon = {
                            Icon(
                                Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = CoinGold,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        value = "${state.coins}",
                        modifier = Modifier.weight(1f)
                    )
                    // Hints
                    StatChip(
                        icon = {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = HintCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        value = "${state.hints}",
                        modifier = Modifier.weight(1f)
                    )
                    // Hearts
                    StatChip(
                        icon = {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                tint = HeartRed,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        value = "${state.hearts}/${state.maxHearts}",
                        modifier = Modifier.weight(1f)
                    )
                    // Level
                    StatChip(
                        icon = {
                            Icon(
                                Icons.Default.MilitaryTech,
                                contentDescription = null,
                                tint = XpPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        value = "Lv.${state.level}",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Main Play Button ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(64.dp)
                        .clip(MaterialTheme.shapes.large)
                        .clickable { viewModel.showNewGameDialog() }
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Sheen effect
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(80.dp)
                            .offset(x = sheenOffset.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                    Color.White.copy(alpha = 0f),
                                    Color.White.copy(alpha = 0.25f),
                                    Color.White.copy(alpha = 0f)
                                )
                                )
                            )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            "Play",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Daily Challenge Button ---
                if (state.isDailyAvailable) {
                    OutlinedButton(
                        onClick = { navController.navigate(NavRoutes.DAILY_CHALLENGE) },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(48.dp),
                        shape = MaterialTheme.shapes.large,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(
                            Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Daily Challenge",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // --- Practice Mode Button ---
                OutlinedButton(
                    onClick = { viewModel.showPracticeDialog() },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(48.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Outlined.School, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Practice Mode",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Navigation Grid ---
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NavButton(
                        icon = Icons.Outlined.Person,
                        label = "Profile",
                        modifier = Modifier.weight(1f)
                    ) {
                        navController.navigate(NavRoutes.PROFILE)
                    }
                    NavButton(
                        icon = Icons.Outlined.ShoppingCart,
                        label = "Shop",
                        modifier = Modifier.weight(1f)
                    ) {
                        navController.navigate(NavRoutes.SHOP)
                    }
                    NavButton(
                        icon = Icons.Outlined.EmojiEvents,
                        label = "Achieve",
                        modifier = Modifier.weight(1f)
                    ) {
                        navController.navigate(NavRoutes.ACHIEVEMENTS)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NavButton(
                        icon = Icons.Outlined.Leaderboard,
                        label = "Ranks",
                        modifier = Modifier.weight(1f)
                    ) {
                        navController.navigate(NavRoutes.LEADERBOARD)
                    }
                    NavButton(
                        icon = Icons.Outlined.BarChart,
                        label = "Stats",
                        modifier = Modifier.weight(1f)
                    ) {
                        navController.navigate(NavRoutes.STATISTICS)
                    }
                    NavButton(
                        icon = Icons.Outlined.Group,
                        label = "Friends",
                        modifier = Modifier.weight(1f)
                    ) {
                        navController.navigate(NavRoutes.FRIENDS)
                    }
                }

                if (state.daysUntilChest > 0 && state.daysUntilChest <= 5) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = CoinGold.copy(alpha = 0.1f)
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Outlined.CardGiftcard,
                                contentDescription = null,
                                tint = CoinGold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Chest in ${state.daysUntilChest} levels!",
                                fontWeight = FontWeight.Medium,
                                color = CoinGold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun StatChip(
    icon: @Composable () -> Unit,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon()
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                value,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun NavButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}