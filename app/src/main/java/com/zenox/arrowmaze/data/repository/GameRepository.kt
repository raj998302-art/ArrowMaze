package com.zenox.arrowmaze.data.repository

import com.zenox.arrowmaze.data.local.datastore.SettingsDataStore
import com.zenox.arrowmaze.data.local.db.dao.GameStatsDao
import com.zenox.arrowmaze.data.local.db.dao.PlayerDao
import com.zenox.arrowmaze.data.local.db.entity.GameStatsEntity
import com.zenox.arrowmaze.data.local.db.entity.PlayerEntity
import com.zenox.arrowmaze.data.remote.firebase.FirestoreService
import com.zenox.arrowmaze.domain.game.GameEngine
import com.zenox.arrowmaze.domain.game.PuzzleGenerator
import com.zenox.arrowmaze.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    private val playerDao: PlayerDao,
    private val gameStatsDao: GameStatsDao,
    private val puzzleGenerator: PuzzleGenerator,
    private val gameEngine: GameEngine,
    private val firestoreService: FirestoreService,
    private val settingsDataStore: SettingsDataStore
) {

    // --- Profile ---

    fun getPlayerProfile(): Flow<PlayerProfile?> {
        return playerDao.getPlayer().map { it?.toProfile() }
    }

    // --- Puzzle generation ---

    suspend fun generatePuzzle(level: Int, worldSalt: Int): Puzzle {
        return puzzleGenerator.generate(level, worldSalt)
    }

    suspend fun generateDailyPuzzle(dateString: String): Puzzle {
        return puzzleGenerator.generateDaily(dateString)
    }

    suspend fun generatePracticePuzzle(difficulty: GameDifficulty): Puzzle {
        return puzzleGenerator.generatePractice(difficulty)
    }

    // --- Settings ---

    private data class SettingsPartial(
        val musicEnabled: Boolean = true,
        val sfxEnabled: Boolean = true,
        val vibrationEnabled: Boolean = true,
        val colorBlindMode: Boolean = false,
        val largeText: Boolean = false
    )

    fun getSettings(): Flow<AppSettings> {
        val bools = combine(
            settingsDataStore.musicEnabled,
            settingsDataStore.sfxEnabled,
            settingsDataStore.vibrationEnabled,
            settingsDataStore.colorBlindMode,
            settingsDataStore.largeText
        ) { m, s, v, cb, lt -> SettingsPartial(m, s, v, cb, lt) }
        return combine(
            bools,
            settingsDataStore.musicVolume,
            settingsDataStore.sfxVolume,
            settingsDataStore.darkMode,
            settingsDataStore.notificationsEnabled
        ) { partial, mv, sv, dm, ne ->
            AppSettings(
                musicEnabled = partial.musicEnabled,
                sfxEnabled = partial.sfxEnabled,
                vibrationEnabled = partial.vibrationEnabled,
                colorBlindMode = partial.colorBlindMode,
                largeText = partial.largeText,
                musicVolume = mv,
                sfxVolume = sv,
                darkMode = dm,
                notificationsEnabled = ne,
                theme = settingsDataStore.currentTheme.first()
            )
        }
    }

    suspend fun updateSettings(settings: AppSettings) {
        settingsDataStore.setMusicEnabled(settings.musicEnabled)
        settingsDataStore.setSfxEnabled(settings.sfxEnabled)
        settingsDataStore.setVibrationEnabled(settings.vibrationEnabled)
        settingsDataStore.setColorBlindMode(settings.colorBlindMode)
        settingsDataStore.setLargeText(settings.largeText)
        settingsDataStore.setMusicVolume(settings.musicVolume)
        settingsDataStore.setSfxVolume(settings.sfxVolume)
        settingsDataStore.setDarkMode(settings.darkMode)
        settingsDataStore.setNotificationsEnabled(settings.notificationsEnabled)
        settingsDataStore.setCurrentTheme(settings.theme)
    }

    // --- Progress ---

    suspend fun saveProgress(progress: GameProgress, uid: String) {
        val entity = PlayerEntity.fromProgress(progress, uid)
        playerDao.upsert(entity)

        val statsEntity = GameStatsEntity.fromStats(progress.stats)
        gameStatsDao.upsert(statsEntity)

        // Sync to Firestore
        val playerData = mapOf<String, Any?>(
            "coins" to progress.coins,
            "hints" to progress.hints,
            "level" to progress.level,
            "worldSalt" to progress.worldSalt,
            "hearts" to progress.hearts,
            "maxHearts" to progress.maxHearts,
            "lastHeartTimestamp" to progress.lastHeartTimestamp,
            "lastDailyDate" to progress.lastDailyDate,
            "dailyStreak" to progress.dailyStreak,
            "dailyDoneDate" to progress.dailyDoneDate,
            "equippedSkin" to progress.equippedSkin,
            "equippedTrail" to progress.equippedTrail,
            "equippedBg" to progress.equippedBg,
            "ownedThemes" to progress.ownedThemes.joinToString(","),
            "ownedSkins" to progress.ownedSkins.joinToString(","),
            "ownedTrails" to progress.ownedTrails.joinToString(","),
            "ownedBgs" to progress.ownedBgs.joinToString(","),
            "unlockedAchievements" to progress.unlockedAchievements.joinToString(","),
            "stats" to mapOf(
                "levelsCompleted" to progress.stats.levelsCompleted,
                "totalMoves" to progress.stats.totalMoves,
                "perfectLevels" to progress.stats.perfectLevels,
                "hintsUsed" to progress.stats.hintsUsed,
                "playTimeMs" to progress.stats.playTimeMs,
                "totalCoinsEarned" to progress.stats.totalCoinsEarned,
                "bestStreak" to progress.stats.bestStreak,
                "currentStreak" to progress.stats.currentStreak,
                "wrongTaps" to progress.stats.wrongTaps,
                "dailyChallengesCompleted" to progress.stats.dailyChallengesCompleted
            )
        )
        firestoreService.savePlayerData(uid, playerData)
    }

    suspend fun syncFromFirestore(uid: String) {
        val result = firestoreService.loadPlayerData(uid)
        if (result.isSuccess) {
            val data = result.getOrNull() ?: return
            val existingPlayer = playerDao.getPlayer().first() ?: return
            val statsMap = data["stats"] as? Map<*, *>

            val updatedPlayer = existingPlayer.copy(
                coins = (data["coins"] as? Number)?.toInt() ?: existingPlayer.coins,
                hints = (data["hints"] as? Number)?.toInt() ?: existingPlayer.hints,
                level = (data["level"] as? Number)?.toInt() ?: existingPlayer.level,
                worldSalt = (data["worldSalt"] as? Number)?.toInt() ?: existingPlayer.worldSalt,
                hearts = (data["hearts"] as? Number)?.toInt() ?: existingPlayer.hearts,
                maxHearts = (data["maxHearts"] as? Number)?.toInt() ?: existingPlayer.maxHearts,
                lastHeartTimestamp = (data["lastHeartTimestamp"] as? Number)?.toLong() ?: existingPlayer.lastHeartTimestamp,
                lastDailyDate = (data["lastDailyDate"] as? String) ?: existingPlayer.lastDailyDate,
                dailyStreak = (data["dailyStreak"] as? Number)?.toInt() ?: existingPlayer.dailyStreak,
                dailyDoneDate = (data["dailyDoneDate"] as? String) ?: existingPlayer.dailyDoneDate,
                equippedSkin = (data["equippedSkin"] as? String) ?: existingPlayer.equippedSkin,
                equippedTrail = (data["equippedTrail"] as? String) ?: existingPlayer.equippedTrail,
                equippedBg = (data["equippedBg"] as? String) ?: existingPlayer.equippedBg,
                ownedThemes = (data["ownedThemes"] as? String) ?: existingPlayer.ownedThemes,
                ownedSkins = (data["ownedSkins"] as? String) ?: existingPlayer.ownedSkins,
                ownedTrails = (data["ownedTrails"] as? String) ?: existingPlayer.ownedTrails,
                ownedBgs = (data["ownedBgs"] as? String) ?: existingPlayer.ownedBgs,
                unlockedAchievements = (data["unlockedAchievements"] as? String) ?: existingPlayer.unlockedAchievements
            )
            playerDao.upsert(updatedPlayer)

            if (statsMap != null) {
                val existingStats = gameStatsDao.getStats().first()
                if (existingStats != null) {
                    val mergedStats = existingStats.copy(
                        levelsCompleted = (statsMap["levelsCompleted"] as? Number)?.toInt() ?: existingStats.levelsCompleted,
                        totalMoves = (statsMap["totalMoves"] as? Number)?.toInt() ?: existingStats.totalMoves,
                        perfectLevels = (statsMap["perfectLevels"] as? Number)?.toInt() ?: existingStats.perfectLevels,
                        hintsUsed = (statsMap["hintsUsed"] as? Number)?.toInt() ?: existingStats.hintsUsed,
                        playTimeMs = (statsMap["playTimeMs"] as? Number)?.toLong() ?: existingStats.playTimeMs,
                        totalCoinsEarned = (statsMap["totalCoinsEarned"] as? Number)?.toLong() ?: existingStats.totalCoinsEarned,
                        bestStreak = (statsMap["bestStreak"] as? Number)?.toInt() ?: existingStats.bestStreak,
                        currentStreak = (statsMap["currentStreak"] as? Number)?.toInt() ?: existingStats.currentStreak,
                        wrongTaps = (statsMap["wrongTaps"] as? Number)?.toInt() ?: existingStats.wrongTaps,
                        dailyChallengesCompleted = (statsMap["dailyChallengesCompleted"] as? Number)?.toInt() ?: existingStats.dailyChallengesCompleted
                    )
                    gameStatsDao.upsert(mergedStats)
                }
            }
        }
    }

    // --- Quick field updates ---

    suspend fun updateCoins(coins: Int, uid: String) {
        playerDao.updateCoins(uid, coins)
    }

    suspend fun updateHints(hints: Int, uid: String) {
        playerDao.updateHints(uid, hints)
    }

    suspend fun updateHearts(hearts: Int, uid: String) {
        playerDao.updateHearts(uid, hearts)
    }

    suspend fun updateLevel(level: Int, uid: String) {
        playerDao.updateLevel(uid, level)
    }

    suspend fun incrementStat(field: String, uid: String, amount: Int) {
        val stats = gameStatsDao.getStats().first() ?: GameStatsEntity()
        val updated = when (field) {
            "levelsCompleted" -> stats.copy(levelsCompleted = stats.levelsCompleted + amount)
            "totalMoves" -> stats.copy(totalMoves = stats.totalMoves + amount)
            "perfectLevels" -> stats.copy(perfectLevels = stats.perfectLevels + amount)
            "hintsUsed" -> stats.copy(hintsUsed = stats.hintsUsed + amount)
            "playTimeMs" -> stats.copy(playTimeMs = stats.playTimeMs + amount)
            "totalCoinsEarned" -> stats.copy(totalCoinsEarned = stats.totalCoinsEarned + amount)
            "currentStreak" -> stats.copy(currentStreak = stats.currentStreak + amount)
            "wrongTaps" -> stats.copy(wrongTaps = stats.wrongTaps + amount)
            "dailyChallengesCompleted" -> stats.copy(dailyChallengesCompleted = stats.dailyChallengesCompleted + amount)
            "bestStreak" -> stats.copy(bestStreak = maxOf(stats.bestStreak, amount))
            else -> return
        }
        gameStatsDao.upsert(updated)
    }

    fun getStats(): Flow<GameStats?> {
        return gameStatsDao.getStats().map { it?.toStats() }
    }

    data class AppSettings(
        val musicEnabled: Boolean,
        val sfxEnabled: Boolean,
        val vibrationEnabled: Boolean,
        val colorBlindMode: Boolean,
        val largeText: Boolean,
        val musicVolume: Float,
        val sfxVolume: Float,
        val darkMode: Boolean,
        val notificationsEnabled: Boolean,
        val theme: String
    )
}