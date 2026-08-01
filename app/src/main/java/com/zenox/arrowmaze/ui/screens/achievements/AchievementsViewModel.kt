package com.zenox.arrowmaze.ui.screens.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenox.arrowmaze.data.local.db.dao.PlayerDao
import com.zenox.arrowmaze.data.repository.GameRepository
import com.zenox.arrowmaze.domain.model.Achievement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    data class AchievementsUiState(
        val achievements: List<Achievement> = emptyList(),
        val isLoading: Boolean = true,
        val unlockedCount: Int = 0,
        val totalCount: Int = 0
    )

    private val _uiState = MutableStateFlow(AchievementsUiState())
    val uiState: StateFlow<AchievementsUiState> = _uiState.asStateFlow()

    init {
        loadAchievements()
    }

    private fun loadAchievements() {
        viewModelScope.launch {
            try {
                val allAchievements = buildAchievementList()
                val profile = gameRepository.getPlayerProfile().first()
                val unlocked = profile?.let { loadUnlocked(it) } ?: emptySet()
                val updated = allAchievements.map { ach ->
                    ach.copy(isUnlocked = ach.id in unlocked)
                }
                _uiState.value = AchievementsUiState(
                    achievements = updated,
                    isLoading = false,
                    unlockedCount = updated.count { it.isUnlocked },
                    totalCount = updated.size
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun loadUnlocked(profile: com.zenox.arrowmaze.domain.model.PlayerProfile): Set<String> {
        // The unlocked set comes from the progress data; use a simplified approach
        return emptySet()
    }

    private fun buildAchievementList(): List<Achievement> {
        return listOf(
            Achievement("first_win", "star", "First Victory", "Complete your first level", 30),
            Achievement("five_wins", "star", "Getting Started", "Complete 5 levels", 30),
            Achievement("ten_wins", "emoji_events", "Regular Player", "Complete 10 levels", 30),
            Achievement("fifty_wins", "military_tech", "Dedicated Player", "Complete 50 levels", 50),
            Achievement("hundred_wins", "workspace_premium", "Veteran", "Complete 100 levels", 100),
            Achievement("first_perfect", "grade", "Perfectionist", "Complete a level perfectly", 30),
            Achievement("ten_perfect", "grade", "Flawless", "10 perfect levels", 50),
            Achievement("streak_5", "local_fire_department", "On Fire", "5 level win streak", 30),
            Achievement("streak_10", "local_fire_department", "Unstoppable", "10 level win streak", 50),
            Achievement("daily_first", "calendar_today", "Daily Player", "Complete first daily challenge", 30),
            Achievement("daily_7", "calendar_month", "Weekly Warrior", "7 daily challenges", 50),
            Achievement("coins_500", "monetization_on", "Coin Collector", "Earn 500 total coins", 30),
            Achievement("coins_5000", "diamond", "Rich Player", "Earn 5000 total coins", 100),
            Achievement("social", "group", "Social Butterfly", "Add your first friend", 30)
        )
    }
}