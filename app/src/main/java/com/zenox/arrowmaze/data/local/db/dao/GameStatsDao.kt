package com.zenox.arrowmaze.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zenox.arrowmaze.data.local.db.entity.GameStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stats: GameStatsEntity)

    @Query("SELECT * FROM game_stats WHERE id = 1")
    fun getStats(): Flow<GameStatsEntity?>

    @Query("UPDATE game_stats SET levelsCompleted = :value WHERE id = 1")
    suspend fun updateLevelsCompleted(value: Int)

    @Query("UPDATE game_stats SET totalMoves = :value WHERE id = 1")
    suspend fun updateTotalMoves(value: Int)

    @Query("UPDATE game_stats SET perfectLevels = :value WHERE id = 1")
    suspend fun updatePerfectLevels(value: Int)
}