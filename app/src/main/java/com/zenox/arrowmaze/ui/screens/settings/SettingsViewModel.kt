package com.zenox.arrowmaze.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenox.arrowmaze.data.repository.AuthRepository
import com.zenox.arrowmaze.data.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    data class SettingsUiState(
        val musicEnabled: Boolean = true,
        val sfxEnabled: Boolean = true,
        val vibrationEnabled: Boolean = true,
        val colorBlindMode: Boolean = false,
        val largeText: Boolean = false,
        val musicVolume: Float = 0.7f,
        val sfxVolume: Float = 0.8f,
        val darkMode: Boolean = false,
        val notificationsEnabled: Boolean = true,
        val currentTheme: String = "light",
        val showAbout: Boolean = false,
        val showPrivacyPolicy: Boolean = false,
        val showResetDataDialog: Boolean = false,
        val playerName: String = ""
    )

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val settings = gameRepository.getSettings().first()
                val profile = gameRepository.getPlayerProfile().first()
                _uiState.value = SettingsUiState(
                    musicEnabled = settings.musicEnabled,
                    sfxEnabled = settings.sfxEnabled,
                    vibrationEnabled = settings.vibrationEnabled,
                    colorBlindMode = settings.colorBlindMode,
                    largeText = settings.largeText,
                    musicVolume = settings.musicVolume,
                    sfxVolume = settings.sfxVolume,
                    darkMode = settings.darkMode,
                    notificationsEnabled = settings.notificationsEnabled,
                    currentTheme = settings.theme,
                    playerName = profile?.playerName ?: ""
                )
            } catch (_: Exception) {}
        }
    }

    fun updateMusicEnabled(value: Boolean) {
        _uiState.value = _uiState.value.copy(musicEnabled = value)
        saveSetting { it.copy(musicEnabled = value) }
    }

    fun updateSfxEnabled(value: Boolean) {
        _uiState.value = _uiState.value.copy(sfxEnabled = value)
        saveSetting { it.copy(sfxEnabled = value) }
    }

    fun updateVibrationEnabled(value: Boolean) {
        _uiState.value = _uiState.value.copy(vibrationEnabled = value)
        saveSetting { it.copy(vibrationEnabled = value) }
    }

    fun updateColorBlindMode(value: Boolean) {
        _uiState.value = _uiState.value.copy(colorBlindMode = value)
        saveSetting { it.copy(colorBlindMode = value) }
    }

    fun updateLargeText(value: Boolean) {
        _uiState.value = _uiState.value.copy(largeText = value)
        saveSetting { it.copy(largeText = value) }
    }

    fun updateMusicVolume(value: Float) {
        _uiState.value = _uiState.value.copy(musicVolume = value)
        saveSetting { it.copy(musicVolume = value) }
    }

    fun updateSfxVolume(value: Float) {
        _uiState.value = _uiState.value.copy(sfxVolume = value)
        saveSetting { it.copy(sfxVolume = value) }
    }

    fun updateDarkMode(value: Boolean) {
        _uiState.value = _uiState.value.copy(darkMode = value)
        saveSetting { it.copy(darkMode = value) }
    }

    fun updateNotificationsEnabled(value: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = value)
        saveSetting { it.copy(notificationsEnabled = value) }
    }

    fun updateCurrentTheme(theme: String) {
        _uiState.value = _uiState.value.copy(currentTheme = theme)
        saveSetting { it.copy(theme = theme) }
    }

    private fun saveSetting(updater: (GameRepository.AppSettings) -> GameRepository.AppSettings) {
        viewModelScope.launch {
            try {
                val current = gameRepository.getSettings().first()
                val updated = updater(current)
                gameRepository.updateSettings(updated)
            } catch (_: Exception) {}
        }
    }

    fun showAbout() { _uiState.value = _uiState.value.copy(showAbout = true) }
    fun dismissAbout() { _uiState.value = _uiState.value.copy(showAbout = false) }
    fun showPrivacyPolicy() { _uiState.value = _uiState.value.copy(showPrivacyPolicy = true) }
    fun dismissPrivacyPolicy() { _uiState.value = _uiState.value.copy(showPrivacyPolicy = false) }
    fun showResetDataDialog() { _uiState.value = _uiState.value.copy(showResetDataDialog = true) }
    fun dismissResetDataDialog() { _uiState.value = _uiState.value.copy(showResetDataDialog = false) }
}