package com.zenox.arrowmaze.ui.screens.game.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zenox.arrowmaze.ui.theme.CoinGold
import com.zenox.arrowmaze.ui.theme.XpPurple

@Composable
fun LevelCompleteDialog(
    coinsEarned: Int,
    isPerfect: Boolean,
    isChestLevel: Boolean,
    isMilestone: Boolean,
    onNext: () -> Unit,
    onHome: () -> Unit
) {
    // Bounce animation for trophy
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Trophy with bounce
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = CoinGold,
                    modifier = Modifier
                        .size(64.dp)
                        .offset(y = bounce.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Level Complete!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Coins earned
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = CoinGold,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        "+$coinsEarned",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = CoinGold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Perfect badge
                if (isPerfect) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(300)) + scaleIn(tween(300, initialScale = 0.8f))
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("★ Perfect!", fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Star, contentDescription = null, tint = CoinGold) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = CoinGold.copy(alpha = 0.15f)
                            )
                        )
                    }
                }

                // Milestone badge
                if (isMilestone) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(400, delayMillis = 200)) + scaleIn(tween(400, initialScale = 0.8f))
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("🏆 Milestone!", fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = XpPurple) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = XpPurple.copy(alpha = 0.15f)
                            )
                        )
                    }
                }

                // Chest reward
                if (isChestLevel) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(500, delayMillis = 400)) + scaleIn(tween(500, initialScale = 0.8f))
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("🎁 Chest Reward!", fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Next button
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("Next", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Home button
                TextButton(
                    onClick = onHome,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Home, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Home", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
