package com.zenox.arrowmaze.domain.model

data class PlayerProfile(
    val uid: String = "",
    val nickname: String = "",
    val playerName: String = "",
    val avatarUrl: String = "",
    val country: String = "Global",
    val bio: String = "",
    val joinDate: String = "",
    val level: Int = 1,
    val xp: Long = 0,
    val xpToNext: Long = 200,
    val coins: Int = 150,
    val hints: Int = 5,
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val bestStreak: Int = 0,
    val avgSolveTimeMs: Long = 0,
    val isAnonymous: Boolean = true,
    val isEmailVerified: Boolean = false,
    val currentTheme: String = "light",
    val currentSkin: String = "classic",
    val currentTrail: String = "sparkle"
)