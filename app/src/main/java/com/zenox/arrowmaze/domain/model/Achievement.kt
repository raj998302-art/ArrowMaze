package com.zenox.arrowmaze.domain.model

data class Achievement(
    val id: String,
    val icon: String,
    val name: String,
    val description: String,
    val coinReward: Int = 30,
    val isUnlocked: Boolean = false
)