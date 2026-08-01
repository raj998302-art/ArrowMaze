package com.zenox.arrowmaze.ui.screens.dailychallenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenox.arrowmaze.core.util.Constants
import com.zenox.arrowmaze.core.util.DateUtils
import com.zenox.arrowmaze.data.local.db.dao.PlayerDao
import com.zenox.arrowmaze.data.repository.AuthRepository
import com.zenox.arrowmaze.data.repository.GameRepository
import com.zenox.arrowmaze.domain.model.DailyChallenge
import com.zenox.arrowmaze.domain.model.GameDifficulty
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DailyChallengeViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val authRepository: AuthRepository,
    private val playerDao: PlayerDao
) : ViewModel() {

    data class DailyUiState(
        val isCompleted: Boolean = false,
        val date: String = "",
        val difficulty: GameDifficulty = GameDifficulty.EASY,
        val coinsReward: Int = 0,
        val hintsReward: Int = 0,
        val isLoading: Boolean = true,
        val navigateToGame: Boolean = false,
        val gameLevel: Int = 0,
        val gameWorldSalt: Int = -1
    )

    private val _uiState = MutableStateFlow(DailyUiState())
    val uiState: StateFlow<DailyUiState> = _uiState.asStateFlow()

    init {
        loadDailyChallenge()
    }

    private fun loadDailyChallenge() {
        viewModelScope.launch {
            try {
                val today = DateUtils.todayString()
                val player = playerDao.getPlayer().first()
                val isCompleted = player?.dailyDoneDate == today

                val dayNum = today.hashCode().let { if (it < 0) -it else it }
                val level = 5 + (dayNum % 40)
                val difficulty = GameDifficulty.fromLevel(level)

                _uiState.value = DailyUiState(
                    isCompleted = isCompleted,
                    date = today,
                    difficulty = difficulty,
                    coinsReward = (difficulty.baseCoins * Constants.DAILY_MULTIPLIER).toInt() + Constants.DAILY_BONUS_COINS,
                    hintsReward = Constants.DAILY_BONUS_HINTS,
                    isLoading = false
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun startDailyChallenge() {
        val state = _uiState.value
        // Navigate with a special salt indicating daily challenge
        _uiState.value = state.copy(
            navigateToGame = true,
            gameLevel = 0, // 0 signals daily
            gameWorldSalt = -1 // -1 signals daily
        )
    }
}