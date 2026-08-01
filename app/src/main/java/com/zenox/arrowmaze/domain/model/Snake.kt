package com.zenox.arrowmaze.domain.model

import com.zenox.arrowmaze.domain.game.Cell
import com.zenox.arrowmaze.domain.game.Direction

data class Snake(
    val id: Int,
    val cells: List<Cell>,
    val direction: Direction,
    val colorIndex: Int
) {
    val head: Cell get() = cells.first()
    val tail: List<Cell> get() = cells.drop(1)
    val length: Int get() = cells.size

    fun contains(cell: Cell): Boolean = cells.contains(cell)

    fun occupies(cell: Cell): Boolean = cells.any { it.row == cell.row && it.col == cell.col }
}