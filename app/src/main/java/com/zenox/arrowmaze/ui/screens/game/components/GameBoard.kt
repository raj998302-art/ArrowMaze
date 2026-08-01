package com.zenox.arrowmaze.ui.screens.game.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.zenox.arrowmaze.domain.game.Direction
import com.zenox.arrowmaze.domain.game.GameState
import com.zenox.arrowmaze.domain.model.Puzzle
import com.zenox.arrowmaze.ui.theme.*

@Composable
fun GameBoard(
    puzzle: Puzzle,
    gameState: GameState?,
    removingSnakeId: Int?,
    wrongSnakeId: Int?,
    currentTheme: String,
    currentSkin: String,
    currentTrail: String,
    onCellTap: (row: Int, col: Int) -> Unit
) {
    val boardColors = gameColorsForTheme(currentTheme)
    val density = LocalDensity.current
    val cellPaddingPx = with(density) { 2.dp.toPx() }
    val cornerRadius = with(density) { 6.dp.toPx() }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .aspectRatio(puzzle.cols.toFloat() / puzzle.rows.toFloat())
            .clip(RoundedCornerShape(12.dp))
            .pointerInput(puzzle.rows, puzzle.cols) {
                detectTapGestures { offset ->
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val cellW = canvasWidth / puzzle.cols
                    val cellH = canvasHeight / puzzle.rows
                    val col = (offset.x / cellW).toInt().coerceIn(0, puzzle.cols - 1)
                    val row = (offset.y / cellH).toInt().coerceIn(0, puzzle.rows - 1)
                    onCellTap(row, col)
                }
            }
    ) {
        val canvasW = size.width
        val canvasH = size.height
        val cellW = canvasW / puzzle.cols
        val cellH = canvasH / puzzle.rows

        // Draw board background
        drawRoundRect(
            color = boardColors.boardBackground,
            size = Size(canvasW, canvasH),
            cornerRadius = CornerRadius(cornerRadius)
        )

        val remainingSnakes = gameState?.remainingSnakes ?: puzzle.snakes
        val hintedId = gameState?.hintedSnakeId

        // Draw each snake
        for (snake in remainingSnakes) {
            val snakeColor = boardColors.snakeColors[snake.colorIndex % boardColors.snakeColors.size]
            val isRemoving = snake.id == removingSnakeId
            val isWrong = snake.id == wrongSnakeId
            val isHinted = snake.id == hintedId

            for (cell in snake.cells) {
                val x = cell.col * cellW + cellPaddingPx
                val y = cell.row * cellH + cellPaddingPx
                val w = cellW - cellPaddingPx * 2
                val h = cellH - cellPaddingPx * 2

                val alpha = if (isRemoving) 0.3f else 1f
                val fillColor = when {
                    isWrong -> Color.Red.copy(alpha = alpha)
                    isHinted -> CoinGold.copy(alpha = alpha)
                    else -> snakeColor.copy(alpha = alpha)
                }

                drawRoundRect(
                    color = fillColor,
                    topLeft = Offset(x, y),
                    size = Size(w, h),
                    cornerRadius = CornerRadius(cornerRadius * 0.6f),
                    alpha = alpha
                )

                // Hinted border
                if (isHinted) {
                    drawRoundRect(
                        color = CoinGold,
                        topLeft = Offset(x, y),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(cornerRadius * 0.6f),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                // Wrong border
                if (isWrong) {
                    drawRoundRect(
                        color = Color.Red,
                        topLeft = Offset(x, y),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(cornerRadius * 0.6f),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }

            // Draw arrow on head cell
            val head = snake.head
            val headX = head.col * cellW + cellPaddingPx
            val headY = head.row * cellH + cellPaddingPx
            val headW = cellW - cellPaddingPx * 2
            val headH = cellH - cellPaddingPx * 2

            val arrowColor = Color.White
            val cx = headX + headW / 2f
            val cy = headY + headH / 2f
            val arrowSize = minOf(headW, headH) * 0.35f

            val path = Path().apply {
                when (snake.direction) {
                    Direction.UP -> {
                        moveTo(cx, cy - arrowSize)
                        lineTo(cx - arrowSize * 0.7f, cy + arrowSize * 0.5f)
                        lineTo(cx + arrowSize * 0.7f, cy + arrowSize * 0.5f)
                        close()
                    }
                    Direction.DOWN -> {
                        moveTo(cx, cy + arrowSize)
                        lineTo(cx - arrowSize * 0.7f, cy - arrowSize * 0.5f)
                        lineTo(cx + arrowSize * 0.7f, cy - arrowSize * 0.5f)
                        close()
                    }
                    Direction.LEFT -> {
                        moveTo(cx - arrowSize, cy)
                        lineTo(cx + arrowSize * 0.5f, cy - arrowSize * 0.7f)
                        lineTo(cx + arrowSize * 0.5f, cy + arrowSize * 0.7f)
                        close()
                    }
                    Direction.RIGHT -> {
                        moveTo(cx + arrowSize, cy)
                        lineTo(cx - arrowSize * 0.5f, cy - arrowSize * 0.7f)
                        lineTo(cx - arrowSize * 0.5f, cy + arrowSize * 0.7f)
                        close()
                    }
                }
            }

            val pathAlpha = if (isRemoving) 0.3f else 1f
            drawPath(
                path = path,
                color = arrowColor,
                alpha = pathAlpha
            )
        }

        // Draw grid lines
        val lineColor = boardColors.cellLineColor
        for (r in 0..puzzle.rows) {
            val y = r * cellH
            drawLine(
                color = lineColor,
                start = Offset(0f, y),
                end = Offset(canvasW, y),
                strokeWidth = 0.5.dp.toPx()
            )
        }
        for (c in 0..puzzle.cols) {
            val x = c * cellW
            drawLine(
                color = lineColor,
                start = Offset(x, 0f),
                end = Offset(x, canvasH),
                strokeWidth = 0.5.dp.toPx()
            )
        }
    }
}
