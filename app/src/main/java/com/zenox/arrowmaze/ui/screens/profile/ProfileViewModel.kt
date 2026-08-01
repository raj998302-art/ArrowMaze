package com.zenox.arrowmaze.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenox.arrowmaze.data.repository.AuthRepository
import com.zenox.arrowmaze.data.repository.GameRepository
import com.zenox.arrowmaze.domain.model.PlayerProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    data class ProfileUiState(
        val profile: PlayerProfile? = null,
        val isLoading: Boolean = true,
        val isEditing: Boolean = false,
        val editNickname: String = "",
        val editCountry: String = "",
        val editBio: String = "",
        val isAnonymous: Boolean = true,
        val isEmailVerified: Boolean = false,
        val showLogoutDialog: Boolean = false,
        val showDeleteAccountDialog: Boolean = false,
        val showEmailLoginDialog: Boolean = false,
        val emailInput: String = "",
        val passwordInput: String = "",
        val showError: String? = null
    )

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                val profile = gameRepository.getPlayerProfile().first()
                val isAnon = authRepository.isAnonymous
                _uiState.value = _uiState.value.copy(
                    profile = profile,
                    isLoading = false,
                    isAnonymous = isAnon,
                    isEmailVerified = profile?.isEmailVerified ?: false,
                    editNickname = profile?.nickname ?: "",
                    editCountry = profile?.country ?: "",
                    editBio = profile?.bio ?: ""
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun startEditing() {
        val profile = _uiState.value.profile ?: return
        _uiState.value = _uiState.value.copy(
            isEditing = true,
            editNickname = profile.nickname,
            editCountry = profile.country,
            editBio = profile.bio
        )
    }

    fun updateEditNickname(value: String) {
        _uiState.value = _uiState.value.copy(editNickname = value)
    }

    fun updateEditCountry(value: String) {
        _uiState.value = _uiState.value.copy(editCountry = value)
    }

    fun updateEditBio(value: String) {
        _uiState.value = _uiState.value.copy(editBio = value)
    }

    fun saveProfile() {
        viewModelScope.launch {
            val state = _uiState.value
            val result = authRepository.updateProfile(
                nickname = state.editNickname,
                country = state.editCountry,
                bio = state.editBio
            )
            if (result.isSuccess) {
                _uiState.value = state.copy(isEditing = false, showError = null)
                loadProfile()
            } else {
                _uiState.value = state.copy(
                    showError = result.exceptionOrNull()?.message ?: "Failed to save"
                )
            }
        }
    }

    fun cancelEditing() {
        _uiState.value = _uiState.value.copy(isEditing = false, showError = null)
    }

    fun showEmailLoginDialog() {
        _uiState.value = _uiState.value.copy(showEmailLoginDialog = true)
    }

    fun dismissEmailLoginDialog() {
        _uiState.value = _uiState.value.copy(showEmailLoginDialog = false, showError = null)
    }

    fun updateEmailInput(value: String) {
        _uiState.value = _uiState.value.copy(emailInput = value)
    }

    fun updatePasswordInput(value: String) {
        _uiState.value = _uiState.value.copy(passwordInput = value)
    }

    fun linkEmailAccount() {
        viewModelScope.launch {
            val state = _uiState.value
            val result = authRepository.linkEmailAccount(state.emailInput, state.passwordInput)
            if (result.isSuccess) {
                _uiState.value = state.copy(
                    showEmailLoginDialog = false,
                    isAnonymous = false,
                    emailInput = "",
                    passwordInput = "",
                    showError = null
                )
                loadProfile()
            } else {
                _uiState.value = state.copy(
                    showError = result.exceptionOrNull()?.message ?: "Failed to link email"
                )
            }
        }
    }

    fun showLogoutDialog() {
        _uiState.value = _uiState.value.copy(showLogoutDialog = true)
    }

    fun dismissLogoutDialog() {
        _uiState.value = _uiState.value.copy(showLogoutDialog = false)
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.value = _uiState.value.copy(showLogoutDialog = false)
        }
    }

    fun showDeleteAccountDialog() {
        _uiState.value = _uiState.value.copy(showDeleteAccountDialog = true)
    }

    fun dismissDeleteAccountDialog() {
        _uiState.value = _uiState.value.copy(showDeleteAccountDialog = false)
    }

    fun deleteAccount() {
        viewModelScope.launch {
            authRepository.deleteAccount()
            _uiState.value = _uiState.value.copy(showDeleteAccountDialog = false)
        }
    }
}