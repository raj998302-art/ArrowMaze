package com.zenox.arrowmaze.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zenox.arrowmaze.data.local.db.dao.GameStatsDao
import com.zenox.arrowmaze.data.local.db.dao.PlayerDao
import com.zenox.arrowmaze.data.local.db.entity.GameStatsEntity
import com.zenox.arrowmaze.data.local.db.entity.PlayerEntity

@Database(
    entities = [
        PlayerEntity::class,
        GameStatsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao

    abstract fun gameStatsDao(): GameStatsDao
}