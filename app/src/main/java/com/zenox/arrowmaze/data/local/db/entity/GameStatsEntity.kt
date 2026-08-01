package com.zenox.arrowmaze.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zenox.arrowmaze.domain.model.GameStats

@Entity(tableName = "game_stats")
data class GameStatsEntity(
    @PrimaryKey
    val id: Int = 1,
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
) {

    fun toStats(): GameStats {
        return GameStats(
            levelsCompleted = levelsCompleted,
            totalMoves = totalMoves,
            perfectLevels = perfectLevels,
            hintsUsed = hintsUsed,
            playTimeMs = playTimeMs,
            totalCoinsEarned = totalCoinsEarned,
            bestStreak = bestStreak,
            currentStreak = currentStreak,
            wrongTaps = wrongTaps,
            dailyChallengesCompleted = dailyChallengesCompleted
        )
    }

    companion object {
        fun fromStats(stats: GameStats): GameStatsEntity {
            return GameStatsEntity(
                id = 1,
                levelsCompleted = stats.levelsCompleted,
                totalMoves = stats.totalMoves,
                perfectLevels = stats.perfectLevels,
                hintsUsed = stats.hintsUsed,
                playTimeMs = stats.playTimeMs,
                totalCoinsEarned = stats.totalCoinsEarned,
                bestStreak = stats.bestStreak,
                currentStreak = stats.currentStreak,
                wrongTaps = stats.wrongTaps,
                dailyChallengesCompleted = stats.dailyChallengesCompleted
            )
        }
    }
}