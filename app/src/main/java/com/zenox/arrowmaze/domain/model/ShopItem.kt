package com.zenox.arrowmaze.domain.model

enum class ShopCategory(val key: String, val label: String) {
    THEMES("themes", "Themes"),
    ARROW_SKINS("skins", "Arrow Skins"),
    TRAIL_FX("trails", "Trail FX"),
    BOARD_BGS("bgs", "Board Backgrounds"),
    HINT_PACKS("hints", "Hint Packs"),
    COIN_PACKS("coins", "Coin Packs")
}

data class ShopItem(
    val id: String,
    val category: ShopCategory,
    val name: String,
    val price: Int = 0,
    val isFree: Boolean = false,
    val isPremium: Boolean = false,
    val isOwned: Boolean = false,
    val isEquipped: Boolean = false,
    val previewColor: Long = 0xFF3B6CFF
)