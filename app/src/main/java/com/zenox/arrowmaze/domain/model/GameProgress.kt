package com.zenox.arrowmaze.domain.model

data class GameProgress(
    val coins: Int = 150,
    val hints: Int = 5,
    val level: Int = 1,
    val worldSalt: Int = 0,
    val hearts: Int = 3,
    val maxHearts: Int = 3,
    val lastHeartTimestamp: Long = 0L,
    val lastDailyDate: String = "",
    val dailyStreak: Int = 0,
    val dailyDoneDate: String = "",
    val joinDate: String = "",
    val stats: GameStats = GameStats(),
    val ownedThemes: Set<String> = setOf("light", "dark"),
    val ownedSkins: Set<String> = setOf("classic"),
    val ownedTrails: Set<String> = setOf("sparkle"),
    val ownedBgs: Set<String> = setOf("grid"),
    val equippedSkin: String = "classic",
    val equippedTrail: String = "sparkle",
    val equippedBg: String = "grid",
    val unlockedAchievements: Set<String> = emptySet()
)