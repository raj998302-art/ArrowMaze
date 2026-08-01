package com.zenox.arrowmaze.domain.model

enum class GameDifficulty(val index: Int, val label: String, val baseCoins: Int, val color: Long) {
    EASY(0, "Easy", 10, 0xFF4CAF50),
    NORMAL(1, "Normal", 20, 0xFF2196F3),
    HARD(2, "Hard", 40, 0xFFFF9800),
    EXPERT(3, "Expert", 70, 0xFFF44336),
    MASTER(4, "Master", 100, 0xFF9C27B0),
    LEGEND(5, "Legend", 120, 0xFF607D8B);

    companion object {
        fun fromLevel(level: Int): GameDifficulty = when {
            level <= 10 -> EASY
            level <= 30 -> NORMAL
            level <= 60 -> HARD
            level <= 100 -> EXPERT
            level <= 200 -> MASTER
            else -> LEGEND
        }

        fun fromPractice(index: Int): GameDifficulty = entries.getOrElse(index) { EASY }
    }
}