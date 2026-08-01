package com.zenox.arrowmaze.data.repository

import com.zenox.arrowmaze.data.local.db.dao.PlayerDao
import com.zenox.arrowmaze.data.remote.firebase.FirestoreService
import com.zenox.arrowmaze.domain.model.ShopCategory
import com.zenox.arrowmaze.domain.model.ShopItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopRepository @Inject constructor(
    private val playerDao: PlayerDao,
    private val firestoreService: FirestoreService
) {

    suspend fun getAllShopItems(): List<ShopItem> {
        val player = playerDao.getPlayer().map { it }.first() ?: return buildAllDefinitions()
        val ownedThemes = player.ownedThemes.split(",").filter { it.isNotBlank() }.toSet()
        val ownedSkins = player.ownedSkins.split(",").filter { it.isNotBlank() }.toSet()
        val ownedTrails = player.ownedTrails.split(",").filter { it.isNotBlank() }.toSet()
        val ownedBgs = player.ownedBgs.split(",").filter { it.isNotBlank() }.toSet()

        return buildAllDefinitions().map { item ->
            val isOwned = when (item.category) {
                ShopCategory.THEMES -> item.id in ownedThemes
                ShopCategory.ARROW_SKINS -> item.id in ownedSkins
                ShopCategory.TRAIL_FX -> item.id in ownedTrails
                ShopCategory.BOARD_BGS -> item.id in ownedBgs
                else -> false
            }
            val isEquipped = when (item.category) {
                ShopCategory.THEMES -> item.id == player.currentTheme
                ShopCategory.ARROW_SKINS -> item.id == player.equippedSkin
                ShopCategory.TRAIL_FX -> item.id == player.equippedTrail
                ShopCategory.BOARD_BGS -> item.id == player.equippedBg
                else -> false
            }
            item.copy(isOwned = isOwned, isEquipped = isEquipped)
        }
    }

    suspend fun getItemsByCategory(category: ShopCategory): List<ShopItem> {
        return getAllShopItems().filter { it.category == category }
    }

    suspend fun purchaseItem(item: ShopItem, uid: String): Result<Unit> = runCatching {
        val player = playerDao.getPlayer().map { it }.first()
            ?: throw IllegalStateException("No player data found")
        if (player.coins < item.price) throw IllegalStateException("Not enough coins")
        if (item.isOwned) throw IllegalStateException("Item already owned")

        val newCoins = player.coins - item.price
        val updatedPlayer = when (item.category) {
            ShopCategory.THEMES -> player.copy(
                coins = newCoins,
                ownedThemes = (player.ownedThemes.split(",").filter { it.isNotBlank() }.toSet() + item.id).joinToString(",")
            )
            ShopCategory.ARROW_SKINS -> player.copy(
                coins = newCoins,
                ownedSkins = (player.ownedSkins.split(",").filter { it.isNotBlank() }.toSet() + item.id).joinToString(",")
            )
            ShopCategory.TRAIL_FX -> player.copy(
                coins = newCoins,
                ownedTrails = (player.ownedTrails.split(",").filter { it.isNotBlank() }.toSet() + item.id).joinToString(",")
            )
            ShopCategory.BOARD_BGS -> player.copy(
                coins = newCoins,
                ownedBgs = (player.ownedBgs.split(",").filter { it.isNotBlank() }.toSet() + item.id).joinToString(",")
            )
            else -> player.copy(coins = newCoins)
        }
        playerDao.upsert(updatedPlayer)

        firestoreService.savePlayerData(uid, mapOf(
            "coins" to newCoins,
            "ownedThemes" to updatedPlayer.ownedThemes,
            "ownedSkins" to updatedPlayer.ownedSkins,
            "ownedTrails" to updatedPlayer.ownedTrails,
            "ownedBgs" to updatedPlayer.ownedBgs
        ))
    }

    suspend fun equipItem(item: ShopItem, uid: String): Result<Unit> = runCatching {
        val player = playerDao.getPlayer().map { it }.first()
            ?: throw IllegalStateException("No player data found")

        val updatedPlayer = when (item.category) {
            ShopCategory.THEMES -> player.copy(currentTheme = item.id)
            ShopCategory.ARROW_SKINS -> player.copy(equippedSkin = item.id)
            ShopCategory.TRAIL_FX -> player.copy(equippedTrail = item.id)
            ShopCategory.BOARD_BGS -> player.copy(equippedBg = item.id)
            else -> player
        }
        playerDao.upsert(updatedPlayer)

        firestoreService.savePlayerData(uid, mapOf(
            "currentTheme" to updatedPlayer.currentTheme,
            "equippedSkin" to updatedPlayer.equippedSkin,
            "equippedTrail" to updatedPlayer.equippedTrail,
            "equippedBg" to updatedPlayer.equippedBg
        ))
    }

    // --- Definitions ---

    fun getThemeDefinitions(): List<ShopItem> = listOf(
        ShopItem("light", ShopCategory.THEMES, "Light", 0, isFree = true, previewColor = 0xFFFAFAFA),
        ShopItem("dark", ShopCategory.THEMES, "Dark", 0, isFree = true, previewColor = 0xFF212121),
        ShopItem("neon", ShopCategory.THEMES, "Neon", 400, previewColor = 0xFF00E676),
        ShopItem("cyberpunk", ShopCategory.THEMES, "Cyberpunk", 400, previewColor = 0xFFFF00FF),
        ShopItem("minimal", ShopCategory.THEMES, "Minimal", 250, previewColor = 0xFFBDBDBD),
        ShopItem("wood", ShopCategory.THEMES, "Wood", 300, previewColor = 0xFF8D6E63),
        ShopItem("space", ShopCategory.THEMES, "Space", 500, previewColor = 0xFF311B92),
        ShopItem("glass", ShopCategory.THEMES, "Glass", 600, previewColor = 0xFF80DEEA),
        ShopItem("ocean", ShopCategory.THEMES, "Ocean", 450, previewColor = 0xFF0277BD),
        ShopItem("forest", ShopCategory.THEMES, "Forest", 350, previewColor = 0xFF2E7D32),
        ShopItem("sunset", ShopCategory.THEMES, "Sunset", 400, previewColor = 0xFFFF6F00),
        ShopItem("golden", ShopCategory.THEMES, "Golden", 500, previewColor = 0xFFFFD600)
    )

    fun getSkinDefinitions(): List<ShopItem> = listOf(
        ShopItem("classic", ShopCategory.ARROW_SKINS, "Classic", 0, isFree = true, previewColor = 0xFF3B6CFF),
        ShopItem("bold_arrow", ShopCategory.ARROW_SKINS, "Bold Arrow", 200, previewColor = 0xFFD32F2F),
        ShopItem("chevron", ShopCategory.ARROW_SKINS, "Chevron", 200, previewColor = 0xFF7B1FA2),
        ShopItem("dart", ShopCategory.ARROW_SKINS, "Dart", 350, previewColor = 0xFFFFC107)
    )

    fun getTrailDefinitions(): List<ShopItem> = listOf(
        ShopItem("sparkle", ShopCategory.TRAIL_FX, "Sparkle", 0, isFree = true, previewColor = 0xFFE040FB),
        ShopItem("bubbles", ShopCategory.TRAIL_FX, "Bubbles", 150, previewColor = 0xFF00BCD4),
        ShopItem("stars", ShopCategory.TRAIL_FX, "Stars", 250, previewColor = 0xFFFFD600),
        ShopItem("fire", ShopCategory.TRAIL_FX, "Fire", 350, previewColor = 0xFFFF5722)
    )

    fun getBgDefinitions(): List<ShopItem> = listOf(
        ShopItem("grid", ShopCategory.BOARD_BGS, "Grid", 0, isFree = true, previewColor = 0xFFE0E0E0),
        ShopItem("dots", ShopCategory.BOARD_BGS, "Dots", 120, previewColor = 0xFFBDBDBD),
        ShopItem("lines", ShopCategory.BOARD_BGS, "Lines", 120, previewColor = 0xFF90A4AE),
        ShopItem("plain", ShopCategory.BOARD_BGS, "Plain", 80, previewColor = 0xFFF5F5F5)
    )

    fun getHintPacks(): List<ShopItem> = listOf(
        ShopItem("hints_3", ShopCategory.HINT_PACKS, "3 Hints", 100, previewColor = 0xFF4CAF50),
        ShopItem("hints_10", ShopCategory.HINT_PACKS, "10 Hints", 280, previewColor = 0xFF4CAF50),
        ShopItem("hints_25", ShopCategory.HINT_PACKS, "25 Hints", 600, previewColor = 0xFF4CAF50)
    )

    fun getCoinPacks(): List<ShopItem> = listOf(
        ShopItem("coins_100", ShopCategory.COIN_PACKS, "100 Coins", 0, isFree = true, isPremium = false, previewColor = 0xFFFFC107),
        ShopItem("coins_500", ShopCategory.COIN_PACKS, "500 Coins", 0, isFree = true, isPremium = false, previewColor = 0xFFFF9800),
        ShopItem("coins_2000", ShopCategory.COIN_PACKS, "2000 Coins", 0, isFree = true, isPremium = false, previewColor = 0xFFFF5722)
    )

    private fun buildAllDefinitions(): List<ShopItem> {
        return getThemeDefinitions() +
                getSkinDefinitions() +
                getTrailDefinitions() +
                getBgDefinitions() +
                getHintPacks() +
                getCoinPacks()
    }
}