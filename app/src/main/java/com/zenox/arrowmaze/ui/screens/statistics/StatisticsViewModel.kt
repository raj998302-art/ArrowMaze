package com.zenox.arrowmaze.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenox.arrowmaze.data.repository.GameRepository
import com.zenox.arrowmaze.domain.model.GameStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    data class StatisticsUiState(
        val levelsCompleted: Int = 0,
        val totalMoves: Int = 0,
        val perfectLevels: Int = 0,
        val hintsUsed: Int = 0,
        val playTimeMs: Long = 0,
        val totalCoinsEarned: Long = 0,
        val bestStreak: Int = 0,
        val currentStreak: Int = 0,
        val wrongTaps: Int = 0,
        val dailyChallengesCompleted: Int = 0,
        val isLoading: Boolean = true
    )

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            try {
                val stats = gameRepository.getStats().first() ?: GameStats()
                _uiState.value = StatisticsUiState(
                    levelsCompleted = stats.levelsCompleted,
                    totalMoves = stats.totalMoves,
                    perfectLevels = stats.perfectLevels,
                    hintsUsed = stats.hintsUsed,
                    playTimeMs = stats.playTimeMs,
                    totalCoinsEarned = stats.totalCoinsEarned,
                    bestStreak = stats.bestStreak,
                    currentStreak = stats.currentStreak,
                    wrongTaps = stats.wrongTaps,
                    dailyChallengesCompleted = stats.dailyChallengesCompleted,
                    isLoading = false
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}