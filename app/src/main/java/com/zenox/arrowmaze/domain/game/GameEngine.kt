package com.zenox.arrowmaze.domain.game

import com.zenox.arrowmaze.domain.model.Puzzle
import com.zenox.arrowmaze.domain.model.Snake
import com.zenox.arrowmaze.core.util.Constants

data class GameState(
    val puzzle: Puzzle,
    val remainingSnakes: List<Snake>,
    val hearts: Int,
    val maxHearts: Int,
    val moves: Int,
    val wrongMoves: Int,
    val hintsUsed: Int,
    val isComplete: Boolean,
    val isGameOver: Boolean,
    val hintsRemaining: Int,
    val hintedSnakeId: Int?,
    val startTime: Long,
    val elapsedTime: Long,
    val isDaily: Boolean,
    val isPractice: Boolean
)

data class TapResult(
    val isValid: Boolean,
    val removedSnake: Snake?,
    val errorMessage: String? = null
)

class GameEngine {

    fun createInitialState(
        puzzle: Puzzle,
        hearts: Int,
        hints: Int,
        isDaily: Boolean = false,
        isPractice: Boolean = false
    ): GameState {
        return GameState(
            puzzle = puzzle,
            remainingSnakes = puzzle.snakes.toList(),
            hearts = hearts,
            maxHearts = Constants.MAX_HEARTS,
            moves = 0,
            wrongMoves = 0,
            hintsUsed = 0,
            isComplete = false,
            isGameOver = false,
            hintsRemaining = hints,
            hintedSnakeId = null,
            startTime = System.currentTimeMillis(),
            elapsedTime = 0L,
            isDaily = isDaily,
            isPractice = isPractice
        )
    }

    fun tapCell(state: GameState, row: Int, col: Int): TapResult {
        if (state.isComplete || state.isGameOver) {
            return TapResult(false, null, "Game is not active")
        }

        val tappedCell = Cell(row, col)
        val tappedSnake = state.remainingSnakes.find { it.occupies(tappedCell) }
            ?: return TapResult(false, null, "No snake at this position")

        if (!isSnakeRemovable(state, tappedSnake)) {
            val newHearts = state.hearts - 1
            val isGameOver = newHearts <= 0
            return TapResult(
                isValid = false,
                removedSnake = null,
                errorMessage = if (isGameOver) "game_over" else "blocked"
            )
        }

        return TapResult(isValid = true, removedSnake = tappedSnake)
    }

    fun removeSnake(state: GameState, snake: Snake): GameState {
        val newRemaining = state.remainingSnakes.filter { it.id != snake.id }
        val isComplete = newRemaining.isEmpty()
        return state.copy(
            remainingSnakes = newRemaining,
            moves = state.moves + 1,
            isComplete = isComplete,
            hintedSnakeId = null
        )
    }

    fun applyWrongTap(state: GameState): GameState {
        val newHearts = state.hearts - 1
        return state.copy(
            hearts = newHearts,
            wrongMoves = state.wrongMoves + 1,
            isGameOver = newHearts <= 0
        )
    }

    fun useHint(state: GameState): GameState? {
        if (state.hintsRemaining <= 0) return null

        val removableSnake = state.remainingSnakes.firstOrNull {
            isSnakeRemovable(state, it)
        } ?: return null

        return state.copy(
            hintsRemaining = state.hintsRemaining - 1,
            hintsUsed = state.hintsUsed + 1,
            hintedSnakeId = removableSnake.id
        )
    }

    fun restoreHearts(state: GameState): GameState {
        return state.copy(
            hearts = state.maxHearts,
            isGameOver = false
        )
    }

    fun updateElapsedTime(state: GameState): GameState {
        return state.copy(
            elapsedTime = System.currentTimeMillis() - state.startTime
        )
    }

    private fun isSnakeRemovable(state: GameState, snake: Snake): Boolean {
        val head = snake.head
        var current = head.move(snake.direction)
        while (current.inBounds(state.puzzle.rows, state.puzzle.cols)) {
            val occupant = state.remainingSnakes.find {
                it.id != snake.id && it.occupies(current)
            }
            if (occupant != null) return false
            current = current.move(snake.direction)
        }
        return true
    }

    fun calculateRewards(state: GameState): Int {
        val difficulty = state.puzzle.difficulty
        var coins = difficulty.baseCoins
        if (state.wrongMoves == 0 && state.hintsUsed == 0) {
            coins += Constants.PERFECT_BONUS_COINS
        }
        if (state.puzzle.level > 0 && state.puzzle.level % Constants.MILESTONE_INTERVAL == 0) {
            coins += Constants.MILESTONE_COINS
        }
        if (state.puzzle.level > 0 && state.puzzle.level % Constants.CHEST_INTERVAL == 0) {
            coins += Constants.CHEST_COINS
        }
        if (state.isDaily) {
            coins = (coins * Constants.DAILY_MULTIPLIER).toInt() + Constants.DAILY_BONUS_COINS
        }
        return coins
    }

    fun isChestLevel(state: GameState): Boolean {
        return state.puzzle.level > 0 && state.puzzle.level % Constants.CHEST_INTERVAL == 0
    }
}