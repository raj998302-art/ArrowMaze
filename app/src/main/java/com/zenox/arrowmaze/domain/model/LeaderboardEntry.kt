package com.zenox.arrowmaze.domain.model

data class LeaderboardEntry(
    val uid: String = "",
    val rank: Int = 0,
    val playerName: String = "",
    val avatarUrl: String = "",
    val country: String = "Global",
    val level: Int = 0,
    val coins: Int = 0,
    val xp: Long = 0,
    val isCurrentUser: Boolean = false
)