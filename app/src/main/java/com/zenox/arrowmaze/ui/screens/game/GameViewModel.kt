package com.zenox.arrowmaze.ui.screens.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenox.arrowmaze.core.util.Constants
import com.zenox.arrowmaze.data.local.db.dao.PlayerDao
import com.zenox.arrowmaze.data.repository.AuthRepository
import com.zenox.arrowmaze.data.repository.GameRepository
import com.zenox.arrowmaze.domain.game.GameEngine
import com.zenox.arrowmaze.domain.game.GameState
import com.zenox.arrowmaze.domain.game.PuzzleGenerator
import com.zenox.arrowmaze.domain.model.*
import com.zenox.arrowmaze.manager.AudioManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val gameEngine: GameEngine,
    private val puzzleGenerator: PuzzleGenerator,
    private val audioManager: AudioManager,
    private val authRepository: AuthRepository,
    private val playerDao: PlayerDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    data class GameUiState(
        val puzzle: Puzzle? = null,
        val gameState: GameState? = null,
        val isLoading: Boolean = true,
        val showLevelComplete: Boolean = false,
        val showGameOver: Boolean = false,
        val earnedCoins: Int = 0,
        val isPerfect: Boolean = false,
        val isChestLevel: Boolean = false,
        val isMilestone: Boolean = false,
        val isDaily: Boolean = false,
        val isPractice: Boolean = false,
        val hintActive: Boolean = false,
        val removingSnakeId: Int? = null,
        val wrongSnakeId: Int? = null,
        val showAdReward: Boolean = false,
        val currentTheme: String = "light",
        val currentSkin: String = "classic",
        val currentTrail: String = "sparkle"
    )

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var currentLevel: Int = 1
    private var currentWorldSalt: Int = 0

    init {
        val level: Int = savedStateHandle["level"] ?: 1
        val worldSalt: Int = savedStateHandle["worldSalt"] ?: 0
        currentLevel = level
        currentWorldSalt = worldSalt
        val isPractice = (level == 0)
        _uiState.value = _uiState.value.copy(isPractice = isPractice)
        loadPuzzle(level, worldSalt, isPractice)
    }

    private fun loadPuzzle(level: Int, worldSalt: Int, isPractice: Boolean = false) {
        viewModelScope.launch {
            try {
                val player = playerDao.getPlayer().first()
                val theme = player?.currentTheme ?: "light"
                val skin = player?.equippedSkin ?: "classic"
                val trail = player?.equippedTrail ?: "sparkle"
                _uiState.value = _uiState.value.copy(
                    currentTheme = theme,
                    currentSkin = skin,
                    currentTrail = trail
                )

                val puzzle = if (isPractice) {
                    val difficulty = GameDifficulty.fromPractice(worldSalt)
                    puzzleGenerator.generatePractice(difficulty)
                } else {
                    puzzleGenerator.generate(level, worldSalt)
                }

                val hearts = player?.hearts ?: Constants.MAX_HEARTS
                val hints = player?.hints ?: 0
                val isDaily = false

                val gameState = gameEngine.createInitialState(
                    puzzle = puzzle,
                    hearts = hearts,
                    hints = hints,
                    isDaily = isDaily,
                    isPractice = isPractice
                )

                _uiState.value = _uiState.value.copy(
                    puzzle = puzzle,
                    gameState = gameState,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onCellTap(row: Int, col: Int) {
        val state = _uiState.value
        val gameState = state.gameState ?: return

        val result = gameEngine.tapCell(gameState, row, col)

        if (result.isValid && result.removedSnake != null) {
            onSnakeRemoved(result.removedSnake)
        } else {
            onWrongTap(row, col)
        }
    }

    private fun onSnakeRemoved(snake: com.zenox.arrowmaze.domain.model.Snake) {
        val state = _uiState.value
        val gameState = state.gameState ?: return

        audioManager.playSound(AudioManager.Sound.TAP)

        // Show removing animation
        _uiState.value = state.copy(removingSnakeId = snake.id)

        viewModelScope.launch {
            delay(Constants.SNAKE_REMOVE_DURATION_MS)

            val newState = gameEngine.removeSnake(gameState, snake)
            val newState2 = gameEngine.updateElapsedTime(newState)

            _uiState.value = _uiState.value.copy(
                gameState = newState2,
                removingSnakeId = null
            )

            if (newState2.isComplete) {
                onLevelComplete(newState2)
            }
        }
    }

    private fun onWrongTap(row: Int, col: Int) {
        val state = _uiState.value
        val gameState = state.gameState ?: return

        audioManager.playSound(AudioManager.Sound.WRONG)

        // Find the snake at this position to flash it
        val tappedSnake = gameState.remainingSnakes.find { it.occupies(com.zenox.arrowmaze.domain.game.Cell(row, col)) }
        _uiState.value = state.copy(wrongSnakeId = tappedSnake?.id)

        viewModelScope.launch {
            delay(Constants.WRONG_SHAKE_DURATION_MS)
            val newState = gameEngine.applyWrongTap(gameState)
            val newState2 = gameEngine.updateElapsedTime(newState)

            _uiState.value = _uiState.value.copy(
                gameState = newState2,
                wrongSnakeId = null
            )

            if (newState2.isGameOver) {
                onGameOver()
            }
        }
    }

    private fun onLevelComplete(gameState: GameState) {
        val coins = gameEngine.calculateRewards(gameState)
        val isPerfect = gameState.wrongMoves == 0 && gameState.hintsUsed == 0
        val isChest = gameEngine.isChestLevel(gameState)
        val isMilestone = gameState.puzzle.level > 0 && gameState.puzzle.level % Constants.MILESTONE_INTERVAL == 0

        audioManager.playSound(AudioManager.Sound.VICTORY)

        _uiState.value = _uiState.value.copy(
            showLevelComplete = true,
            earnedCoins = coins,
            isPerfect = isPerfect,
            isChestLevel = isChest,
            isMilestone = isMilestone
        )

        viewModelScope.launch {
            val uid = authRepository.getUserUid() ?: return@launch
            val player = playerDao.getPlayer().first() ?: return@launch

            val newLevel = if (!_uiState.value.isPractice) player.level + 1 else player.level
            val newWorldSalt = if (!_uiState.value.isPractice) {
                if (isChest) player.worldSalt + 1 else player.worldSalt
            } else player.worldSalt

            val updatedPlayer = player.copy(
                coins = player.coins + coins,
                level = newLevel,
                worldSalt = newWorldSalt,
                hearts = player.maxHearts
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

            gameRepository.incrementStat("levelsCompleted", uid, 1)
            gameRepository.incrementStat("totalMoves", uid, gameState.moves)
            gameRepository.incrementStat("totalCoinsEarned", uid, coins)
            if (isPerfect) gameRepository.incrementStat("perfectLevels", uid, 1)
            if (gameState.hintsUsed > 0) gameRepository.incrementStat("hintsUsed", uid, gameState.hintsUsed)
            gameRepository.incrementStat("wrongTaps", uid, gameState.wrongMoves)
            gameRepository.incrementStat("playTimeMs", uid, gameState.elapsedTime.toInt())

            if (isChest) {
                playerDao.upsert(updatedPlayer.copy(hints = updatedPlayer.hints + Constants.CHEST_HINTS))
            }
        }
    }

    private fun onGameOver() {
        _uiState.value = _uiState.value.copy(showGameOver = true)
    }

    fun useHint() {
        val state = _uiState.value
        val gameState = state.gameState ?: return
        val newState = gameEngine.useHint(gameState) ?: return

        audioManager.playSound(AudioManager.Sound.HINT)
        _uiState.value = state.copy(
            gameState = newState,
            hintActive = true
        )

        viewModelScope.launch {
            delay(Constants.HINT_DURATION_MS)
            val fadedState = _uiState.value.gameState?.copy(hintedSnakeId = null)
            _uiState.value = _uiState.value.copy(
                gameState = fadedState,
                hintActive = false
            )
        }
    }

    fun nextLevel() {
        if (_uiState.value.isPractice) return
        currentLevel += 1
        _uiState.value = GameUiState(
            isPractice = false,
            currentTheme = _uiState.value.currentTheme,
            currentSkin = _uiState.value.currentSkin,
            currentTrail = _uiState.value.currentTrail
        )
        loadPuzzle(currentLevel, currentWorldSalt)
    }

    fun restartLevel() {
        val state = _uiState.value
        _uiState.value = GameUiState(
            isPractice = state.isPractice,
            isDaily = state.isDaily,
            currentTheme = state.currentTheme,
            currentSkin = state.currentSkin,
            currentTrail = state.currentTrail
        )
        loadPuzzle(currentLevel, currentWorldSalt, state.isPractice)
    }

    fun goHome() {
        // Navigation handled by screen
    }

    fun watchAdForHearts() {
        val state = _uiState.value
        val gameState = state.gameState ?: return
        val restored = gameEngine.restoreHearts(gameState)
        _uiState.value = state.copy(
            gameState = restored,
            showGameOver = false
        )
    }

    fun watchAdForCoins() {
        // Ad reward for extra coins - handled by screen/ads manager
    }
}