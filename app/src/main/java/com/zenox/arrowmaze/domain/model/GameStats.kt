package com.zenox.arrowmaze.domain.model

data class GameStats(
    val levelsCompleted: Int = 0,
    val totalMoves: Int = 0,
    val perfectLevels: Int = 0,
    val hintsUsed: Int = 0,
    val playTimeMs: Long = 0L,
    val totalCoinsEarned: Long = 0L,
    val bestStreak: Int = 0,
    val currentStreak: Int = 0,
    val wrongTaps: Int = 0,
    val dailyChallengesCompleted: Int = 0
)