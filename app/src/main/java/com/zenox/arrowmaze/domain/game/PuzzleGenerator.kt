package com.zenox.arrowmaze.domain.game

import com.zenox.arrowmaze.domain.model.GameDifficulty
import com.zenox.arrowmaze.domain.model.Puzzle
import com.zenox.arrowmaze.domain.model.Snake
import com.zenox.arrowmaze.core.util.SeededRandom
import com.zenox.arrowmaze.core.util.Constants
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.math.sqrt

class PuzzleGenerator {

    private fun generatePuzzleInternal(level: Int, difficulty: GameDifficulty, seed: Long): Puzzle {
        val rng = SeededRandom(seed)

        val targetSnakes = min(
            max(round(4.0 * sqrt(level.toDouble())).toInt(), 4),
            150
        )

        val gridSize = min(
            max(ceil(sqrt((targetSnakes * 3).toDouble())).toInt(), Constants.MIN_GRID_SIZE),
            Constants.MAX_GRID_SIZE
        )

        val rows = gridSize
        val cols = gridSize

        val grid = Array(rows) { IntArray(cols) { -1 } }
        val snakes = mutableListOf<Snake>()
        val occupied = mutableSetOf<Pair<Int, Int>>()

        for (attempt in 0 until Constants.PUZZLE_GENERATION_RETRIES) {
            snakes.clear()
            occupied.clear()
            for (r in 0 until rows) for (c in 0 until cols) grid[r][c] = -1

            var snakeId = 0
            for (placement in 0 until targetSnakes * 3) {
                if (snakes.size >= targetSnakes &&
                    (occupied.size.toDouble() / (rows * cols)) >= Constants.BOARD_COVERAGE_TARGET
                ) break

                val startRow = rng.nextInt(rows)
                val startCol = rng.nextInt(cols)
                if (occupied.contains(Pair(startRow, startCol))) continue

                val headCell = Cell(startRow, startCol)
                val validDirections = Direction.entries.filter { dir ->
                    checkExitRay(grid, rows, cols, headCell, dir, snakeId)
                }

                if (validDirections.isEmpty()) continue

                val chosenDir = validDirections[rng.nextInt(validDirections.size)]
                val snakeLength = Constants.MIN_SNAKE_LENGTH +
                    rng.nextInt(Constants.MAX_SNAKE_LENGTH - Constants.MIN_SNAKE_LENGTH + 1)

                val bodyDir = chosenDir.opposite()
                val snakeCells = mutableListOf(headCell)
                var current = headCell

                for (i in 1 until snakeLength) {
                    val next = current.move(bodyDir)
                    if (next.inBounds(rows, cols) && !occupied.contains(Pair(next.row, next.col))) {
                        snakeCells.add(next)
                        current = next
                    } else {
                        break
                    }
                }

                val snake = Snake(
                    id = snakeId,
                    cells = snakeCells.toList(),
                    direction = chosenDir,
                    colorIndex = rng.nextInt(8)
                )

                for (cell in snakeCells) {
                    grid[cell.row][cell.col] = snakeId
                    occupied.add(Pair(cell.row, cell.col))
                }

                snakes.add(snake)
                snakeId++
            }

            if (snakes.isNotEmpty()) return Puzzle(rows, cols, snakes, level, difficulty, seed)
        }

        // Fallback: trivial 3x3 puzzle
        val fallbackSnake = Snake(0, listOf(Cell(0, 0)), Direction.RIGHT, 0)
        return Puzzle(3, 3, listOf(fallbackSnake), level, difficulty, seed)
    }

    private fun checkExitRay(
        grid: Array<IntArray>,
        rows: Int,
        cols: Int,
        head: Cell,
        direction: Direction,
        currentSnakeId: Int
    ): Boolean {
        var current = head.move(direction)
        while (current.inBounds(rows, cols)) {
            val occupant = grid[current.row][current.col]
            if (occupant != -1 && occupant != currentSnakeId) return false
            current = current.move(direction)
        }
        return true
    }

    fun generate(level: Int, worldSalt: Int = 0): Puzzle {
        val difficulty = GameDifficulty.fromLevel(level)
        val seed = SeededRandom.hashString("arrow-maze-$worldSalt-lv-$level")
        return generatePuzzleInternal(level, difficulty, seed)
    }

    fun generateDaily(dateString: String): Puzzle {
        val seed = SeededRandom.hashString("daily-$dateString")
        val dayNum = dateString.hashCode().let { if (it < 0) -it else it }
        val level = 5 + (dayNum % 40)
        val difficulty = GameDifficulty.fromLevel(level)
        return generatePuzzleInternal(level, difficulty, seed)
    }

    fun generatePractice(difficulty: GameDifficulty): Puzzle {
        val simulatedLevel = when (difficulty) {
            GameDifficulty.EASY -> 1
            GameDifficulty.NORMAL -> 25
            GameDifficulty.HARD -> 60
            GameDifficulty.EXPERT -> 120
            GameDifficulty.MASTER -> 250
            GameDifficulty.LEGEND -> 450
        }
        val seed = SeededRandom.hashString("practice-${difficulty.name}-${System.currentTimeMillis()}")
        return generatePuzzleInternal(simulatedLevel, difficulty, seed)
    }
}