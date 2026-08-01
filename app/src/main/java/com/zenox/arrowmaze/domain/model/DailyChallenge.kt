package com.zenox.arrowmaze.domain.model

data class DailyChallenge(
    val date: String,
    val level: Int,
    val difficulty: GameDifficulty,
    val isCompleted: Boolean = false,
    val coinsReward: Int = 0,
    val xpReward: Int = 0,
    val hintsReward: Int = 0
)