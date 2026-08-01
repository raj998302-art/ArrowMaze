package com.zenox.arrowmaze.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zenox.arrowmaze.data.local.db.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(player: PlayerEntity)

    @Query("SELECT * FROM player LIMIT 1")
    fun getPlayer(): Flow<PlayerEntity?>

    @Query("UPDATE player SET coins = :coins WHERE uid = :uid")
    suspend fun updateCoins(uid: String, coins: Int)

    @Query("UPDATE player SET hints = :hints WHERE uid = :uid")
    suspend fun updateHints(uid: String, hints: Int)

    @Query("UPDATE player SET hearts = :hearts WHERE uid = :uid")
    suspend fun updateHearts(uid: String, hearts: Int)

    @Query("UPDATE player SET level = :level WHERE uid = :uid")
    suspend fun updateLevel(uid: String, level: Int)

    @Delete
    suspend fun delete(player: PlayerEntity)
}