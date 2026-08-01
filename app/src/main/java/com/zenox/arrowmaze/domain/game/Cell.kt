package com.zenox.arrowmaze.domain.game

data class Cell(val row: Int, val col: Int) {

    fun inBounds(rows: Int, cols: Int): Boolean =
        row in 0 until rows && col in 0 until cols

    fun move(direction: Direction): Cell = Cell(row + direction.dy(), col + direction.dx())

    fun directionTo(other: Cell): Direction = when {
        other.row < row -> Direction.UP
        other.row > row -> Direction.DOWN
        other.col < col -> Direction.LEFT
        other.col > col -> Direction.RIGHT
        else -> Direction.UP
    }

    fun isAdjacent(other: Cell): Boolean {
        val dr = kotlin.math.abs(row - other.row)
        val dc = kotlin.math.abs(col - other.col)
        return (dr + dc) == 1
    }
}
