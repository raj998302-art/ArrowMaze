package com.zenox.arrowmaze.ui.screens.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenox.arrowmaze.data.repository.AuthRepository
import com.zenox.arrowmaze.data.repository.GameRepository
import com.zenox.arrowmaze.data.repository.ShopRepository
import com.zenox.arrowmaze.domain.model.ShopCategory
import com.zenox.arrowmaze.domain.model.ShopItem
import com.zenox.arrowmaze.manager.AudioManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val shopRepository: ShopRepository,
    private val gameRepository: GameRepository,
    private val authRepository: AuthRepository,
    private val audioManager: AudioManager
) : ViewModel() {

    data class ShopUiState(
        val selectedCategory: ShopCategory = ShopCategory.THEMES,
        val items: List<ShopItem> = emptyList(),
        val coins: Int = 0,
        val isLoading: Boolean = true,
        val purchaseMessage: String? = null
    )

    private val _uiState = MutableStateFlow(ShopUiState())
    val uiState: StateFlow<ShopUiState> = _uiState.asStateFlow()

    init {
        loadShop()
    }

    private fun loadShop() {
        viewModelScope.launch {
            try {
                val profile = gameRepository.getPlayerProfile().first()
                _uiState.value = _uiState.value.copy(coins = profile?.coins ?: 0)
                loadCategory(_uiState.value.selectedCategory)
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun loadCategory(category: ShopCategory) {
        val items = shopRepository.getItemsByCategory(category)
        _uiState.value = _uiState.value.copy(items = items)
    }

    fun selectCategory(category: ShopCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        loadCategory(category)
    }

    fun purchaseItem(item: ShopItem) {
        viewModelScope.launch {
            val uid = authRepository.getUserUid() ?: return@launch
            val result = shopRepository.purchaseItem(item, uid)
            if (result.isSuccess) {
                audioManager.playSound(AudioManager.Sound.BUY)
                _uiState.value = _uiState.value.copy(
                    purchaseMessage = "Purchased ${item.name}!",
                    coins = _uiState.value.coins - item.price
                )
                loadCategory(_uiState.value.selectedCategory)
            } else {
                _uiState.value = _uiState.value.copy(
                    purchaseMessage = result.exceptionOrNull()?.message ?: "Failed to purchase"
                )
            }
        }
    }

    fun equipItem(item: ShopItem) {
        viewModelScope.launch {
            val uid = authRepository.getUserUid() ?: return@launch
            val result = shopRepository.equipItem(item, uid)
            if (result.isSuccess) {
                audioManager.playSound(AudioManager.Sound.COIN)
                _uiState.value = _uiState.value.copy(
                    purchaseMessage = "Equipped ${item.name}!"
                )
                loadCategory(_uiState.value.selectedCategory)
            }
        }
    }

    fun dismissMessage() {
        _uiState.value = _uiState.value.copy(purchaseMessage = null)
    }
}