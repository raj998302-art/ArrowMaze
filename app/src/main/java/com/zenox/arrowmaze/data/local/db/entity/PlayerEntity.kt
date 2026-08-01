package com.zenox.arrowmaze.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zenox.arrowmaze.domain.model.GameProgress
import com.zenox.arrowmaze.domain.model.PlayerProfile

@Entity(tableName = "player")
data class PlayerEntity(
    @PrimaryKey
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
    val worldSalt: Int = 0,
    val hearts: Int = 3,
    val maxHearts: Int = 3,
    val lastHeartTimestamp: Long = 0L,
    val lastDailyDate: String = "",
    val dailyStreak: Int = 0,
    val dailyDoneDate: String = "",
    val isAnonymous: Boolean = true,
    val isEmailVerified: Boolean = false,
    val currentTheme: String = "light",
    val equippedSkin: String = "classic",
    val equippedTrail: String = "sparkle",
    val equippedBg: String = "grid",
    val ownedThemes: String = "light,dark",
    val ownedSkins: String = "classic",
    val ownedTrails: String = "sparkle",
    val ownedBgs: String = "grid",
    val unlockedAchievements: String = ""
) {

    fun toProfile(): PlayerProfile {
        return PlayerProfile(
            uid = uid,
            nickname = nickname,
            playerName = playerName,
            avatarUrl = avatarUrl,
            country = country,
            bio = bio,
            joinDate = joinDate,
            level = level,
            xp = xp,
            xpToNext = xpToNext,
            coins = coins,
            hints = hints,
            isAnonymous = isAnonymous,
            isEmailVerified = isEmailVerified,
            currentTheme = currentTheme,
            currentSkin = equippedSkin,
            currentTrail = equippedTrail
        )
    }

    companion object {
        fun fromProgress(progress: GameProgress, uid: String): PlayerEntity {
            return PlayerEntity(
                uid = uid,
                coins = progress.coins,
                hints = progress.hints,
                level = progress.level,
                worldSalt = progress.worldSalt,
                hearts = progress.hearts,
                maxHearts = progress.maxHearts,
                lastHeartTimestamp = progress.lastHeartTimestamp,
                lastDailyDate = progress.lastDailyDate,
                dailyStreak = progress.dailyStreak,
                dailyDoneDate = progress.dailyDoneDate,
                joinDate = progress.joinDate,
                equippedSkin = progress.equippedSkin,
                equippedTrail = progress.equippedTrail,
                equippedBg = progress.equippedBg,
                ownedThemes = progress.ownedThemes.joinToString(","),
                ownedSkins = progress.ownedSkins.joinToString(","),
                ownedTrails = progress.ownedTrails.joinToString(","),
                ownedBgs = progress.ownedBgs.joinToString(","),
                unlockedAchievements = progress.unlockedAchievements.joinToString(",")
            )
        }
    }
}
