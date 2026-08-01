package com.zenox.arrowmaze.domain.model

import com.zenox.arrowmaze.domain.game.Cell

data class Puzzle(
    val rows: Int,
    val cols: Int,
    val snakes: List<Snake>,
    val level: Int,
    val difficulty: GameDifficulty,
    val seed: Long
) {
    val totalSnakes: Int get() = snakes.size

    fun getSnakeAt(row: Int, col: Int): Snake? =
        snakes.find { snake -> snake.occupies(Cell(row, col)) }

    fun getSnakeById(id: Int): Snake? =
        snakes.find { it.id == id }
}