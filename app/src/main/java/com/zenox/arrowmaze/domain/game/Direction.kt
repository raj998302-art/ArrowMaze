package com.zenox.arrowmaze.domain.game

enum class Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT;

    fun opposite(): Direction = when (this) {
        UP -> DOWN
        DOWN -> UP
        LEFT -> RIGHT
        RIGHT -> LEFT
    }

    fun dx(): Int = when (this) {
        LEFT -> -1
        RIGHT -> 1
        else -> 0
    }

    fun dy(): Int = when (this) {
        UP -> -1
        DOWN -> 1
        else -> 0
    }

    fun angle(): Float = when (this) {
        UP -> -90f
        RIGHT -> 0f
        DOWN -> 90f
        LEFT -> 180f
    }

    companion object {
        fun fromAngle(deg: Float): Direction = when {
            deg >= -135f && deg < -45f -> UP
            deg >= -45f && deg < 45f -> RIGHT
            deg >= 45f && deg < 135f -> DOWN
            else -> LEFT
        }
    }
}
