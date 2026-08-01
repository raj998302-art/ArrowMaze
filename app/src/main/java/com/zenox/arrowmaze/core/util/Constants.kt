package com.zenox.arrowmaze.core.util

object Constants {
    const val SAVE_KEY = "arrowMazeSaveV1"
    const val SETTINGS_KEY = "arrowMazeSettings"
    const val HEART_REGEN_MS = 20 * 60 * 1000L // 20 minutes
    const val MAX_HEARTS = 3
    const val STARTING_COINS = 150
    const val STARTING_HINTS = 5
    const val PERFECT_BONUS_COINS = 5
    const val MILESTONE_INTERVAL = 5
    const val MILESTONE_COINS = 50
    const val CHEST_INTERVAL = 20
    const val CHEST_COINS = 200
    const val CHEST_HINTS = 3
    const val DAILY_MULTIPLIER = 1.5
    const val DAILY_BONUS_COINS = 10
    const val DAILY_BONUS_HINTS = 2
    const val XP_PER_LEVEL = 50
    const val XP_PER_ACHIEVEMENT = 100
    const val XP_TO_NEXT_LEVEL_BASE = 200
    const val ACHIEVEMENT_COIN_REWARD = 30
    const val MAX_DAILY_STREAK = 30
    const val HINT_DURATION_MS = 3500L
    const val LEVEL_COMPLETE_DELAY_MS = 500L
    const val SNAKE_REMOVE_DURATION_MS = 420L
    const val WRONG_SHAKE_DURATION_MS = 380L
    const val MIN_GRID_SIZE = 5
    const val MAX_GRID_SIZE = 14
    const val PUZZLE_GENERATION_RETRIES = 500
    const val BOARD_COVERAGE_TARGET = 0.55
    const val MAX_SNAKE_LENGTH = 4
    const val MIN_SNAKE_LENGTH = 1
    const val LEADERBOARD_PAGE_SIZE = 20
    const val NOTIFICATION_CHANNEL_DAILY = "arrowmaze_daily"
    const val NOTIFICATION_CHANNEL_CHALLENGE = "arrowmaze_challenge"
    const val NOTIFICATION_CHANNEL_REMINDER = "arrowmaze_reminder"
    const val NOTIFICATION_CHANNEL_GENERAL = "arrowmaze_general"

    // Leaderboard time periods
    enum class LeaderboardPeriod { WEEKLY, MONTHLY, ALL_TIME }

    // Ad Placement identifiers
    object AdPlacements {
        const val HOME_BANNER = "home_banner"
        const val GAME_OVER_INTERSTITIAL = "game_over_interstitial"
        const val REWARDED_HINTS = "rewarded_hints"
        const val REWARDED_COINS = "rewarded_coins"
        const val REWARDED_CONTINUE = "rewarded_continue"
        const val SHOP_NATIVE = "shop_native"
        const val APP_OPEN = "app_open"
    }
}