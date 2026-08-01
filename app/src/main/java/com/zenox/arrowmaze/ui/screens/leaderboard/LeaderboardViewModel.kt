package com.zenox.arrowmaze.ui.screens.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenox.arrowmaze.core.util.Constants
import com.zenox.arrowmaze.data.repository.AuthRepository
import com.zenox.arrowmaze.data.repository.LeaderboardRepository
import com.zenox.arrowmaze.domain.model.LeaderboardEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val leaderboardRepository: LeaderboardRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    data class LeaderboardUiState(
        val entries: List<LeaderboardEntry> = emptyList(),
        val selectedPeriod: Constants.LeaderboardPeriod = Constants.LeaderboardPeriod.ALL_TIME,
        val selectedTab: Int = 0,
        val isLoading: Boolean = true,
        val currentUserName: String = ""
    )

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val state = _uiState.value
        if (state.selectedTab == 0) {
            loadGlobal(state.selectedPeriod)
        } else {
            loadFriends()
        }
    }

    private fun loadGlobal(period: Constants.LeaderboardPeriod) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = leaderboardRepository.getGlobalLeaderboard(period)
            val user = authRepository.getUserUid()
            val entries = result.getOrDefault(emptyList()).map { entry ->
                entry.copy(isCurrentUser = entry.uid == user)
            }
            _uiState.value = _uiState.value.copy(
                entries = entries,
                isLoading = false
            )
        }
    }

    private fun loadFriends() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = leaderboardRepository.getFriendsLeaderboard()
            val user = authRepository.getUserUid()
            val entries = result.getOrDefault(emptyList()).map { entry ->
                entry.copy(isCurrentUser = entry.uid == user)
            }
            _uiState.value = _uiState.value.copy(
                entries = entries,
                isLoading = false
            )
        }
    }

    fun selectPeriod(period: Constants.LeaderboardPeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        if (_uiState.value.selectedTab == 0) {
            loadGlobal(period)
        }
    }

    fun selectTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
        loadData()
    }
}