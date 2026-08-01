package com.zenox.arrowmaze.ui.theme

import androidx.compose.ui.graphics.Color

data class GameBoardColors(
    val boardBackground: Color,
    val cellColor: Color,
    val cellLineColor: Color,
    val snakeColors: List<Color>
)

fun gameColorsForTheme(theme: String): GameBoardColors {
    return when (theme) {
        "dark" -> GameBoardColors(
            boardBackground = BoardDark,
            cellColor = Color(0xFF1E2A4A),
            cellLineColor = Color(0xFF2A3A5C),
            snakeColors = listOf(
                Color(0xFF5C9CFF), // lighter blue
                Color(0xFFFF7EB3), // lighter pink
                Color(0xFF69F0AE), // lighter green
                Color(0xFFFFB74D), // lighter orange
                Color(0xFFCE93D8), // lighter purple
                Color(0xFF4DD0E1), // lighter cyan
                Color(0xFFFF8A80), // lighter coral
                Color(0xFFB9F6CA)  // lighter lime
            )
        )
        "neon" -> GameBoardColors(
            boardBackground = Color(0xFF0A0A1A),
            cellColor = Color(0xFF12122A),
            cellLineColor = Color(0xFF1A1A3A),
            snakeColors = listOf(
                Color(0xFF00FFFF), // cyan
                Color(0xFFFF00FF), // magenta
                Color(0xFF00FF66), // lime
                Color(0xFF0088FF), // blue
                Color(0xFFFF8800), // orange
                Color(0xFFAA00FF), // purple
                Color(0xFF00FFAA), // green
                Color(0xFFFFFF00)  // yellow
            )
        )
        "cyberpunk" -> GameBoardColors(
            boardBackground = Color(0xFF1A0A2E),
            cellColor = Color(0xFF2A1A3E),
            cellLineColor = Color(0xFF3A2A5E),
            snakeColors = listOf(
                Color(0xFFFF00FF), // magenta
                Color(0xFFFFE95E), // yellow
                Color(0xFF00FFFF), // cyan
                Color(0xFF00FF88), // green
                Color(0xFFFF8800), // orange
                Color(0xFFAA44FF), // purple
                Color(0xFFFF2EA6), // pink
                Color(0xFF00DDCC)  // teal
            )
        )
        "minimal" -> GameBoardColors(
            boardBackground = Color(0xFFF5F5F5),
            cellColor = Color(0xFFFFFFFF),
            cellLineColor = Color(0xFFE0E0E0),
            snakeColors = listOf(
                Color(0xFF757575), // gray
                Color(0xFF9E9E9E), // light gray
                Color(0xFF616161), // dark gray
                Color(0xFF8D6E63), // brown gray
                Color(0xFF78909C), // blue gray
                Color(0xFFA1887F), // warm gray
                Color(0xFF90A4AE), // cool gray
                Color(0xFFBDBDBD)  // lighter gray
            )
        )
        "space" -> GameBoardColors(
            boardBackground = Color(0xFF0D0D2B),
            cellColor = Color(0xFF151540),
            cellLineColor = Color(0xFF1F1F55),
            snakeColors = listOf(
                Color(0xFF4488FF), // blue
                Color(0xFF9966FF), // purple
                Color(0xFF00CCFF), // cyan
                Color(0xFFFF66AA), // pink
                Color(0xFFFFCC00), // yellow
                Color(0xFF44FF88), // green
                Color(0xFFFF8844), // orange
                Color(0xFFCC99FF)  // lavender
            )
        )
        else -> GameBoardColors( // light (default)
            boardBackground = BoardLight,
            cellColor = Color(0xFFFFFFFF),
            cellLineColor = Color(0xFFD0D8E8),
            snakeColors = listOf(
                Color(0xFF3B6CFF), // blue
                Color(0xFFFF6B9D), // pink
                Color(0xFF4CAF50), // green
                Color(0xFFFF9800), // orange
                Color(0xFF9C27B0), // purple
                Color(0xFF00BCD4), // cyan
                Color(0xFFFF7043), // coral
                Color(0xFF8BC34A)  // lime
            )
        )
    }
}
