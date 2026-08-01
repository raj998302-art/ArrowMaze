package com.zenox.arrowmaze.domain.model

data class FriendRequest(
    val uid: String = "",
    val playerName: String = "",
    val avatarUrl: String = "",
    val status: FriendStatus = FriendStatus.PENDING,
    val timestamp: Long = 0L
)

enum class FriendStatus {
    PENDING,
    ACCEPTED,
    BLOCKED
}