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

    @Query("UPDATE game_stats SET levelsCompleted = :val WHERE id = 1")
    suspend fun updateLevelsCompleted(val: Int)

    @Query("UPDATE game_stats SET totalMoves = :val WHERE id = 1")
    suspend fun updateTotalMoves(val: Int)

    @Query("UPDATE game_stats SET perfectLevels = :val WHERE id = 1")
    suspend fun updatePerfectLevels(val: Int)
}