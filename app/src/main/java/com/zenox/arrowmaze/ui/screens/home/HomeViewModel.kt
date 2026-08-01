package com.zenox.arrowmaze.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenox.arrowmaze.core.util.Constants
import com.zenox.arrowmaze.core.util.DateUtils
import com.zenox.arrowmaze.data.local.db.dao.PlayerDao
import com.zenox.arrowmaze.data.local.db.entity.PlayerEntity
import com.zenox.arrowmaze.data.repository.AuthRepository
import com.zenox.arrowmaze.data.repository.GameRepository
import com.zenox.arrowmaze.domain.model.GameDifficulty
import com.zenox.arrowmaze.domain.model.GameProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val authRepository: AuthRepository,
    private val playerDao: PlayerDao
) : ViewModel() {

    data class HomeUiState(
        val level: Int = 1,
        val worldSalt: Int = 0,
        val coins: Int = 150,
        val hints: Int = 5,
        val hearts: Int = 3,
        val maxHearts: Int = 3,
        val lastHeartTimestamp: Long = 0,
        val dailyStreak: Int = 0,
        val dailyDoneToday: Boolean = false,
        val isDailyAvailable: Boolean = false,
        val showDailyReward: Boolean = false,
        val dailyRewardCoins: Int = 0,
        val dailyRewardHints: Int = 0,
        val playerName: String = "",
        val levelName: String = "",
        val difficultyName: String = "",
        val daysUntilChest: Int = 0,
        val isLoading: Boolean = true,
        val showNewGameDialog: Boolean = false,
        val showPracticeDialog: Boolean = false
    )

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadPlayerData()
    }

    private fun loadPlayerData() {
        viewModelScope.launch {
            try {
                val profile = gameRepository.getPlayerProfile().first()
                if (profile != null) {
                    val difficulty = GameDifficulty.fromLevel(profile.level)
                    val today = DateUtils.todayString()
                    val remaining = if (profile.level % Constants.CHEST_INTERVAL == 0) 0
                        else Constants.CHEST_INTERVAL - (profile.level % Constants.CHEST_INTERVAL)

                    _uiState.value = _uiState.value.copy(
                        level = profile.level,
                        coins = profile.coins,
                        hints = profile.hints,
                        playerName = profile.playerName.ifBlank { profile.nickname.ifBlank { "Player" } },
                        difficultyName = difficulty.label,
                        daysUntilChest = remaining,
                        isLoading = false
                    )

                    val player = playerDao.getPlayer().first()
                    if (player != null) {
                        _uiState.value = _uiState.value.copy(
                            hearts = player.hearts,
                            maxHearts = player.maxHearts,
                            lastHeartTimestamp = player.lastHeartTimestamp,
                            worldSalt = player.worldSalt,
                            dailyStreak = player.dailyStreak,
                            dailyDoneToday = player.dailyDoneDate == today,
                            isDailyAvailable = player.dailyDoneDate != today
                        )
                        checkDailyLogin(player)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private suspend fun checkDailyLogin(player: PlayerEntity) {
        val today = DateUtils.todayString()
        if (player.dailyDoneDate == today) return

        val lastDaily = player.lastDailyDate
        val isConsecutive = DateUtils.isYesterday(lastDaily) || lastDaily.isBlank()

        val newStreak = if (isConsecutive) {
            (player.dailyStreak + 1).coerceAtMost(Constants.MAX_DAILY_STREAK)
        } else {
            1
        }

        val rewardCoins = 10 + (newStreak * 2)
        val rewardHints = if (newStreak % 5 == 0) 2 else 0

        _uiState.value = _uiState.value.copy(
            showDailyReward = true,
            dailyRewardCoins = rewardCoins,
            dailyRewardHints = rewardHints,
            dailyStreak = newStreak
        )
    }

    fun claimDailyReward() {
        viewModelScope.launch {
            val uid = authRepository.getUserUid() ?: return@launch
            val player = playerDao.getPlayer().first() ?: return@launch
            val today = DateUtils.todayString()
            val lastDaily = player.lastDailyDate
            val isConsecutive = DateUtils.isYesterday(lastDaily) || lastDaily.isBlank()

            val newStreak = if (isConsecutive) {
                (player.dailyStreak + 1).coerceAtMost(Constants.MAX_DAILY_STREAK)
            } else {
                1
            }

            val updatedPlayer = player.copy(
                coins = player.coins + _uiState.value.dailyRewardCoins,
                hints = player.hints + _uiState.value.dailyRewardHints,
                dailyStreak = newStreak,
                lastDailyDate = today,
                dailyDoneDate = today
            )
            playerDao.upsert(updatedPlayer)

            gameRepository.saveProgress(
                GameProgress(
                    coins = updatedPlayer.coins,
                    hints = updatedPlayer.hints,
                    level = updatedPlayer.level,
                    worldSalt = updatedPlayer.worldSalt,
                    hearts = updatedPlayer.hearts,
                    maxHearts = updatedPlayer.maxHearts,
                    lastHeartTimestamp = updatedPlayer.lastHeartTimestamp,
                    lastDailyDate = updatedPlayer.lastDailyDate,
                    dailyStreak = updatedPlayer.dailyStreak,
                    dailyDoneDate = updatedPlayer.dailyDoneDate,
                    joinDate = updatedPlayer.joinDate,
                    equippedSkin = updatedPlayer.equippedSkin,
                    equippedTrail = updatedPlayer.equippedTrail,
                    equippedBg = updatedPlayer.equippedBg,
                    ownedThemes = updatedPlayer.ownedThemes.split(",").filter { it.isNotBlank() }.toSet(),
                    ownedSkins = updatedPlayer.ownedSkins.split(",").filter { it.isNotBlank() }.toSet(),
                    ownedTrails = updatedPlayer.ownedTrails.split(",").filter { it.isNotBlank() }.toSet(),
                    ownedBgs = updatedPlayer.ownedBgs.split(",").filter { it.isNotBlank() }.toSet(),
                    unlockedAchievements = updatedPlayer.unlockedAchievements.split(",").filter { it.isNotBlank() }.toSet()
                ),
                uid = uid
            )

            _uiState.value = _uiState.value.copy(
                showDailyReward = false,
                coins = updatedPlayer.coins,
                hints = updatedPlayer.hints,
                dailyStreak = newStreak,
                dailyDoneToday = true,
                isDailyAvailable = false
            )
        }
    }

    fun startGame() {
        _uiState.value = _uiState.value.copy(showNewGameDialog = false)
    }

    fun startDailyChallenge() {}

    fun startPractice(difficulty: GameDifficulty) {
        _uiState.value = _uiState.value.copy(showPracticeDialog = false)
    }

    fun dismissDailyReward() {
        _uiState.value = _uiState.value.copy(showDailyReward = false)
    }

    fun dismissNewGameDialog() {
        _uiState.value = _uiState.value.copy(showNewGameDialog = false)
    }

    fun dismissPracticeDialog() {
        _uiState.value = _uiState.value.copy(showPracticeDialog = false)
    }

    fun showNewGameDialog() {
        _uiState.value = _uiState.value.copy(showNewGameDialog = true)
    }

    fun showPracticeDialog() {
        _uiState.value = _uiState.value.copy(showPracticeDialog = true)
    }

    fun calculateHeartsRegen(): Int {
        val state = _uiState.value
        if (state.hearts >= state.maxHearts) return 0
        if (state.lastHeartTimestamp == 0L) return 0
        val elapsed = System.currentTimeMillis() - state.lastHeartTimestamp
        val heartsRegened = (elapsed / Constants.HEART_REGEN_MS).toInt()
        return heartsRegened.coerceAtMost(state.maxHearts - state.hearts)
    }
}